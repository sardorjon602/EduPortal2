package sfera.eduportal2.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfera.eduportal2.Exception.NotFoundException;
import sfera.eduportal2.Payload.request.ReqRecommendation;
import sfera.eduportal2.Payload.response.ResRecommendation;
import sfera.eduportal2.Repository.ModuleRepository;
import sfera.eduportal2.Repository.RecommendationRepository;
import sfera.eduportal2.Repository.TestResultRepository;
import sfera.eduportal2.Repository.UserRepository;




import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.Recommendation;
import sfera.eduportal2.entity.TestResult;
import sfera.eduportal2.entity.Users;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final TestResultRepository testResultRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ==================== AI: AVTOMATIK TAVSIYA YARATISH ====================

    /**
     * Foydalanuvchining test natijalari va darajasiga qarab
     * Gemini AI orqali eng mos modulni avtomatik tanlaydi va
     * Recommendation sifatida bazaga saqlaydi.
     *
     * @param userId Foydalanuvchi ID si
     * @return Saqlangan tavsiyaning ResRecommendation DTO si
     */
    public ResRecommendation generateAndSave(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: id=" + userId));





        // Foydalanuvchining so'nggi 5 test natijasi
        List<TestResult> results = testResultRepository.findTop5ByUsersOrderByCreatedAtDesc(user);
        double avgScore = results.stream()
                .mapToDouble(TestResult::getScore)
                .average()
                .orElse(0.0);

        // Barcha mavjud modullar ro'yxati
        List<Module> allModules = moduleRepository.findAll();
        if (allModules.isEmpty()) {
            throw new NotFoundException("Tizimda hech qanday modul mavjud emas.");
        }

        // Modul nomlarini raqamlangan ro'yxat sifatida formatlash
        String numberedModuleList = buildNumberedModuleList(allModules);

        // AI ga yuborish uchun prompt
        String prompt = buildRecommendationPrompt(user, avgScore, numberedModuleList);

        // Gemini API dan javob olish
        String aiFullResponse = callGemini(prompt);

        // AI javobidan faqat modul nomini ajratib olish
        String chosenModuleName = extractModuleNameFromAiResponse(aiFullResponse, allModules);

        // Tanlangan modulni topish
        Module chosenModule = allModules.stream()
                .filter(m -> m.getModuleName().equalsIgnoreCase(chosenModuleName))
                .findFirst()
                .orElse(allModules.get(0)); // fallback: birinchi modul

        // Recommendation bazaga saqlash
        Recommendation recommendation = Recommendation.builder()
                .users(user)
                .module(chosenModule)
                .reason(aiFullResponse)
                .build();

        Recommendation saved = recommendationRepository.save(recommendation);
        log.info("AI tavsiya yaratildi va saqlandi: userId={}, module={}", userId, chosenModule.getModuleName());

        return toResponse(saved);
    }

    // ==================== QOLDA TAVSIYA YARATISH (ADMIN) ====================

    /**
     * Admin qo'lda tavsiya kiritadi (AI ishlatilmaydi).
     *
     * @param req userId, moduleName, reason
     * @return yaratilgan tavsiya
     */
    public ResRecommendation create(ReqRecommendation req) {
        Users user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: id=" + req.getUserId()));

        Module module = moduleRepository.findAll().stream()
                .filter(m -> m.getModuleName().equalsIgnoreCase(req.getModuleName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Modul topilmadi: " + req.getModuleName()));

        Recommendation recommendation = Recommendation.builder()
                .users(user)
                .module(module)
                .reason(req.getReason())
                .build();

        Recommendation saved = recommendationRepository.save(recommendation);
        log.info("Qo'lda tavsiya yaratildi: userId={}, module={}", req.getUserId(), req.getModuleName());
        return toResponse(saved);
    }

    // ==================== GET ALL ====================

    public List<ResRecommendation> getAll() {
        return recommendationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== GET BY ID ====================

    public ResRecommendation getById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tavsiya topilmadi: id=" + id));
        return toResponse(recommendation);
    }

    // ==================== GET BY USER ====================

    public List<ResRecommendation> getByUserId(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: id=" + userId));

        return recommendationRepository.findTopByUsersOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE ====================

    public ResRecommendation update(Long id, ReqRecommendation req) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tavsiya topilmadi: id=" + id));

        Users user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: id=" + req.getUserId()));

        Module module = moduleRepository.findAll().stream()
                .filter(m -> m.getModuleName().equalsIgnoreCase(req.getModuleName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Modul topilmadi: " + req.getModuleName()));

        recommendation.setUsers(user);
        recommendation.setModule(module);
        recommendation.setReason(req.getReason());

        Recommendation updated = recommendationRepository.save(recommendation);
        log.info("Tavsiya yangilandi: id={}", id);
        return toResponse(updated);
    }

    // ==================== DELETE ====================

    public void delete(Long id) {
        if (!recommendationRepository.existsById(id)) {
            throw new NotFoundException("Tavsiya topilmadi: id=" + id);
        }
        recommendationRepository.deleteById(id);
        log.info("Tavsiya o'chirildi: id={}", id);
    }

    // ==================== PRIVATE HELPER METODLAR ====================

    /**
     * Modullar ro'yxatini raqamlangan matn sifatida shakllantiradi.
     * AI shu ro'yxatdan birini tanlaydi.
     */
    private String buildNumberedModuleList(List<Module> modules) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            sb.append(i + 1)
                    .append(". ")
                    .append(m.getModuleName())
                    .append(" (")
                    .append(m.getCategory().getName())
                    .append(")\n");
        }
        return sb.toString();
    }

    /**
     * Gemini ga yuboriladigan prompt ni quradi.
     * AI dan FAQAT modul nomini va qisqa sababini so'raydi.
     */
    private String buildRecommendationPrompt(Users user, double avgScore, String numberedModuleList) {
        return String.format(
        "Sen 'EduPortal' onlayn o'quv platformasining AI maslahatchisisan.\n\n" +
        "Foydalanuvchi haqida:\n" +
        "- Ismi: %s\n" +
        "- Hozirgi darajasi: %s\n" +
        "- O'rtacha test bali: %.1f / 100\n\n" +
        "Platformadagi mavjud modullar:\n%s\n" +
        "MUHIM KO'RSATMA:\n" +
        "Yuqoridagi ro'yxatdan faqat BITTA eng mos modulni tanlа.\n" +
        "Javobingni qat'iy quyidagi formatda ber (boshqa hech narsa yozma):\n\n" +
        "MODUL: [modul nomi aynan ro'yxatdagi kabi]\n" +
        "SABAB: [2-3 jumlada foydalanuvchining darajasi va bali asosida nima uchun bu modul mos ekanligini tushuntir]\n\n" +
        "Faqat o'zbek tilida yoz.",
        user.getFullName(),
        user.getLevel().name(),
        avgScore,
        numberedModuleList
        );
    }

    /**
     * AI javobidan "MODUL: ..." qatorini ajratib, modul nomini qaytaradi.
     * Agar ajratib bo'lmasa, eng ko'p mos keladigan modul nomini qidiradi.
     */
    private String extractModuleNameFromAiResponse(String aiResponse, List<Module> allModules) {
        // "MODUL: Java Asoslari" kabi qatorni qidirish
        for (String line : aiResponse.split("\n")) {
            if (line.toUpperCase().startsWith("MODUL:")) {
                String extracted = line.substring(6).trim();
                // Ro'yxatdagi modul nomiga to'liq moslikni tekshirish
                for (Module m : allModules) {
                    if (m.getModuleName().equalsIgnoreCase(extracted)) {
                        return m.getModuleName();
                    }
                }
                // Qisman moslik (AI nom biroz o'zgartirgan bo'lishi mumkin)
                for (Module m : allModules) {
                    if (extracted.toLowerCase().contains(m.getModuleName().toLowerCase())
                            || m.getModuleName().toLowerCase().contains(extracted.toLowerCase())) {
                        return m.getModuleName();
                    }
                }
            }
        }
        // Fallback: AI javob ichida biror modul nomi bormi tekshiramiz
        for (Module m : allModules) {
            if (aiResponse.toLowerCase().contains(m.getModuleName().toLowerCase())) {
                return m.getModuleName();
            }
        }
        log.warn("AI javobidan modul nomi ajratilmadi. Birinchi modul ishlatiladi.");
        return allModules.get(0).getModuleName();
    }

    /**
     * Gemini API ga prompt yuborib, matn javobini qaytaradi.
     */

// ... (qolgan kodlar)

    private String callGemini(String prompt) {
        try {
            String url = GEMINI_URL + apiKey;

            // Xavfsiz JSON yasash (Map orqali)
            Map<String, Object> requestBodyMap = new HashMap<>();

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("parts", List.of(textPart));

            requestBodyMap.put("contents", List.of(partsMap));

            Map<String, Object> generationConfigMap = new HashMap<>();
            generationConfigMap.put("temperature", 0.4);
            generationConfigMap.put("maxOutputTokens", 512);
            requestBodyMap.put("generationConfig", generationConfigMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBodyMap, headers);

            // API ga so'rov yuborish
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return extractTextFromGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("Gemini API xatosi: {}", e.getMessage());
            e.printStackTrace(); // Xatoning asl sababini konsolda ko'rish uchun qo'shildi
            return "Kechirasiz, AI xizmatida xatolik yuz berdi.";
        }
    }
    /**
     * Gemini JSON javobidan faqat text qismini ajratib oladi.
     * candidates[0].content.parts[0].text
     */
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("AI javob bera olmadi.");
        } catch (Exception e) {
            log.warn("Gemini javobini parse qilishda xatolik: {}", e.getMessage());
            return jsonResponse;
        }
    }




//    private String callGemini(String prompt) {
//        try {
//            String url = GEMINI_URL + apiKey;
//
//            String safePrompt = prompt
//                    .replace("\\", "\\\\")
//                    .replace("\"", "\\\"")
//                    .replace("\n", "\\n")
//                    .replace("\r", "");
//
//            String requestBody = "{"
//                    + "\"contents\": [{"
//                    + "\"parts\": [{\"text\": \"" + safePrompt + "\"}]"
//                    + "}],"
//                    + "\"generationConfig\": {"
//                    + "\"temperature\": 0.4,"
//                    + "\"maxOutputTokens\": 512"
//                    + "}"
//                    + "}";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//            return extractTextFromGeminiResponse(response.getBody());
//
//        } catch (Exception e) {
//            log.error("Gemini API xatosi (Recommendation): {}", e.getMessage());
//            return "Kechirasiz, AI xizmatida xatolik yuz berdi.";
//        }
//    }

    /**
     * Recommendation entity ni ResRecommendation DTO ga o'giradi.
     */
    private ResRecommendation toResponse(Recommendation recommendation) {
        return ResRecommendation.builder()
                .id(recommendation.getId())
                .userId(recommendation.getUsers().getId())
                .moduleName(recommendation.getModule().getModuleName())
                .reason(recommendation.getReason())
                .build();
    }
}