//package sfera.eduportal2.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import sfera.eduportal2.Payload.response.ResAiRecommendation;
//import sfera.eduportal2.Repository.ModuleRepository;
//import sfera.eduportal2.Repository.TestResultRepository;
//import sfera.eduportal2.Repository.UserRepository;
//import sfera.eduportal2.entity.Module;
//import sfera.eduportal2.entity.TestResult;
//import sfera.eduportal2.entity.Users;
//import sfera.eduportal2.entity.enums.Level;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EduPortalAiService {
//
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    private static final String GEMINI_URL =
//            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
//
//    private final RestTemplate restTemplate;
//    private final UserRepository userRepository;
//    private final TestResultRepository testResultRepository;
//    private final ModuleRepository moduleRepository;
//    private final ObjectMapper objectMapper;
//
//    // ==================== 1. KURS TAVSIYASI ====================
//
//    /**
//     * O'quvchining test natijalariga va darajasiga qarab
//     * platformadagi modullardan mos kurslarni tavsiya qiladi.
//     *
//     * @param userId O'quvchining ID si
//     * @return AI tavsiyasi va tavsiya etilgan modullar
//     */
//    public ResAiRecommendation recommendCoursesForUser(Long userId) {
//        Optional<Users> userOpt = userRepository.findById(userId);
//        if (userOpt.isEmpty()) {
//            return ResAiRecommendation.error("Foydalanuvchi topilmadi.");
//        }
//        Users user = userOpt.get();
//
//        // Foydalanuvchining so'nggi 5 test natijasini olish
//        List<TestResult> results = testResultRepository.findTop5ByUsersOrderByCreatedAtDesc(user);
//        double avgScore = results.stream()
//                .mapToDouble(TestResult::getScore)
//                .average()
//                .orElse(0.0);
//
//        // Barcha mavjud modullarni olish
//        List<Module> allModules = moduleRepository.findAll();
//        String moduleList = allModules.stream()
//                .map(m -> "- " + m.getModuleName() + " (" + m.getCategory().getName() + ")")
//                .collect(Collectors.joining("\n"));
//
//        String prompt = String.format(
//                "Sen 'EduPortal' onlayn o'quv platformasining AI maslahatchisisan.\n\n" +
//                "O'quvchi haqida ma'lumot:\n" +
//                "- Ismi: %s\n" +
//                "- Hozirgi darajasi: %s\n" +
//                "- O'rtacha test bali: %.1f/100\n\n" +
//                "Platformadagi mavjud modullar:\n%s\n\n" +
//                "Vazifang:\n" +
//                "1. O'quvchining darajasi va test natijasiga qarab, yuqoridagi ro'yxatdan 3 ta eng mos modulni tanlа\n" +
//                "2. Har bir tavsiya uchun qisqacha sabab yoz\n" +
//                "3. O'quvchiga motivatsion xabar yoz\n\n" +
//                "Javobni faqat o'zbek tilida ber. Aniq va qisqa bo'l.",
//                user.getFullName(),
//                user.getLevel().name(),
//                avgScore,
//                moduleList.isEmpty() ? "(hali modullar yo'q)" : moduleList
//        );
//
//        String aiText = callGemini(prompt);
//        return ResAiRecommendation.builder()
//                .userId(userId)
//                .userName(user.getFullName())
//                .userLevel(user.getLevel().name())
//                .averageScore(avgScore)
//                .aiResponse(aiText)
//                .success(true)
//                .build();
//    }
//
//    // ==================== 2. TEST NATIJASINI TAHLIL QILISH ====================
//
//    /**
//     * Test natijasini tahlil qilib, o'quvchiga batafsil fikr-mulohaza beradi.
//     *
//     * @param testResultId TestResult ID si
//     * @return AI tahlili
//     */
//    public ResAiRecommendation analyzeTestResult(Long testResultId) {
//        Optional<TestResult> resultOpt = testResultRepository.findById(testResultId);
//        if (resultOpt.isEmpty()) {
//            return ResAiRecommendation.error("Test natijasi topilmadi.");
//        }
//        TestResult result = resultOpt.get();
//        Users user = result.getUsers();
//
//        String prompt = String.format(
//                "Sen 'EduPortal' platformasining AI o'qituvchisisan.\n\n" +
//                "O'quvchi ma'lumotlari:\n" +
//                "- Ismi: %s\n" +
//                "- Darajasi: %s\n" +
//                "- Test nomi: %s\n" +
//                "- Olingan ball: %.1f/100\n\n" +
//                "Quyidagi bo'limlarda batafsil tahlil ber:\n" +
//                "1. **Natija bahosi** - bu ball qanday darajani bildiradi?\n" +
//                "2. **Kuchli tomonlar** - o'quvchi nimalarni yaxshi biladi (ball asosida taxmin qil)\n" +
//                "3. **Yaxshilash kerak** - qaysi sohalarga e'tibor qaratish kerak?\n" +
//                "4. **Keyingi qadam** - o'quvchi endi nima qilishi kerak?\n" +
//                "5. **Motivatsiya** - qisqacha rag'batlantiruvchi so'z\n\n" +
//                "Faqat o'zbek tilida yoz. Har bir bo'limni yangi qatordan boshlа.",
//                user.getFullName(),
//                user.getLevel().name(),
//                result.getTest().getTitle(),
//                result.getScore()
//        );
//
//        String aiText = callGemini(prompt);
//        return ResAiRecommendation.builder()
//                .userId(user.getId())
//                .userName(user.getFullName())
//                .userLevel(user.getLevel().name())
//                .averageScore(result.getScore())
//                .aiResponse(aiText)
//                .success(true)
//                .build();
//    }
//
//    // ==================== 3. O'QISH YO'L XARITASI ====================
//
//    /**
//     * O'quvchining maqsadi va hozirgi darajasiga qarab
//     * shaxsiy o'qish yo'l xaritasini (learning path) tuzadi.
//     *
//     * @param userId    O'quvchi ID si
//     * @param goal      O'quvchining maqsadi (masalan, "Backend dasturchi bo'lish")
//     * @param interests Qiziqishlar (masalan, "Java, Spring Boot, API")
//     * @return Shaxsiy o'qish rejasi
//     */
//    public ResAiRecommendation generateLearningPath(Long userId, String goal, String interests) {
//        Optional<Users> userOpt = userRepository.findById(userId);
//        if (userOpt.isEmpty()) {
//            return ResAiRecommendation.error("Foydalanuvchi topilmadi.");
//        }
//        Users user = userOpt.get();
//
//        List<Module> allModules = moduleRepository.findAll();
//        String moduleList = allModules.stream()
//                .map(m -> m.getModuleName() + " [" + m.getCategory().getName() + "]")
//                .collect(Collectors.joining(", "));
//
//        String prompt = String.format(
//                "Sen 'EduPortal' platformasida shaxsiy o'quv yo'l xaritasi tuzuvchi AI maslahatchisisan.\n\n" +
//                "O'quvchi haqida:\n" +
//                "- Ismi: %s\n" +
//                "- Hozirgi darajasi: %s\n" +
//                "- Maqsadi: %s\n" +
//                "- Qiziqishlari: %s\n\n" +
//                "Platformadagi mavjud modullar: %s\n\n" +
//                "Topshiriq: Yuqoridagi ma'lumotlarga asoslanib, 3 bosqichli o'quv yo'l xaritasi tuz:\n" +
//                "📌 1-bosqich (Hozir): Qaysi modullardan boshlash kerak va nima uchun?\n" +
//                "📌 2-bosqich (1-2 oy): Qaysi modullarni o'rganish kerak?\n" +
//                "📌 3-bosqich (Maqsad): Maqsadga yetish uchun oxirgi qadamlar\n\n" +
//                "Har bir bosqich uchun taxminiy vaqt va amaliy maslahatlar ber.\n" +
//                "Faqat o'zbek tilida yoz.",
//                user.getFullName(),
//                user.getLevel().name(),
//                goal,
//                interests,
//                moduleList.isEmpty() ? "hali modullar yo'q" : moduleList
//        );
//
//        String aiText = callGemini(prompt);
//        return ResAiRecommendation.builder()
//                .userId(userId)
//                .userName(user.getFullName())
//                .userLevel(user.getLevel().name())
//                .aiResponse(aiText)
//                .success(true)
//                .build();
//    }
//
//    // ==================== GEMINI API CHAQIRUVI ====================
//
//    /**
//     * Gemini API ga so'rov yuborib, faqat text qismini qaytaradi.
//     */
//    private String callGemini(String prompt) {
//        try {
//            String url = GEMINI_URL + apiKey;
//
//            // Prompt ichidagi maxsus belgilarni xavfsizlashtirish
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
//                    + "\"temperature\": 0.7,"
//                    + "\"maxOutputTokens\": 1024"
//                    + "}"
//                    + "}";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//
//            // JSON javobdan faqat text qismini ajratib olamiz
//            return extractTextFromGeminiResponse(response.getBody());
//
//        } catch (Exception e) {
//            log.error("Gemini API xatosi: {}", e.getMessage());
//            return "Kechirasiz, sun'iy intellekt xizmatida xatolik yuz berdi. Iltimos, keyinroq urinib ko'ring.";
//        }
//    }
//
//    /**
//     * Gemini JSON javobidan faqat text qismini ajratib oladi.
//     * candidates[0].content.parts[0].text
//     */
//    private String extractTextFromGeminiResponse(String jsonResponse) {
//        try {
//            JsonNode root = objectMapper.readTree(jsonResponse);
//            return root
//                    .path("candidates")
//                    .get(0)
//                    .path("content")
//                    .path("parts")
//                    .get(0)
//                    .path("text")
//                    .asText("AI javob bera olmadi.");
//        } catch (Exception e) {
//            log.warn("Gemini javobini parse qilishda xatolik: {}", e.getMessage());
//            return jsonResponse; // xom JSON ni qaytaramiz
//        }
//    }
//}