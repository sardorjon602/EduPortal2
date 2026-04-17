package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfera.eduportal2.Repository.ModuleRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ModuleRepository moduleRepository;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // ================================================================
    // TEXT javoblarni tekshirish — har bir savol uchun alohida
    // qaytaradi: Map<questionId, Boolean> — to'g'ri/noto'g'ri
    // ================================================================
    public Map<Long, Boolean> checkTextAnswers(Map<Long, String> questionTextMap,
                                               Map<Long, String> userAnswers) {
        if (questionTextMap.isEmpty()) return new HashMap<>();

        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                Quyidagi savollarga berilgan javoblarni tekshiring.
                Har bir savol uchun faqat "TO'G'RI" yoki "NOTO'G'RI" deb javob bering.
                Javobni FAQAT quyidagi formatda bering, boshqa hech narsa yozmang:
                
                questionId:natija
                
                Misol:
                1:TO'G'RI
                2:NOTO'G'RI
                3:TO'G'RI
                
                Savollar:
                """);

        for (Map.Entry<Long, String> entry : questionTextMap.entrySet()) {
            Long qId = entry.getKey();
            String questionText = entry.getValue();
            String userAnswer = userAnswers.getOrDefault(qId, "Javob berilmagan");
            prompt.append(qId).append(". Savol: ").append(questionText).append("\n");
            prompt.append("   User javobi: ").append(userAnswer).append("\n\n");
        }

        String aiResponse = callGemini(prompt.toString());
        return parseCheckResult(aiResponse, questionTextMap.keySet());
    }

    // ================================================================
    // UMUMIY TAVSIYA — test yakunida
    // ================================================================
    public String getRecommendation(String testSummary) {
        List<String> allModules = moduleRepository.findAll()
                .stream()
                .map(m -> m.getModuleName())
                .toList();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Mavjud modullar: ")
                .append(String.join(", ", allModules)).append("\n\n");
        prompt.append(testSummary);
        prompt.append("""
                \nQuyidagi formatda javob bering:
                1. UMUMIY BAHO: (2-3 jumlada)
                2. ZAIF TOMONLAR: (qaysi modullarda xato ko'p)
                3. TAVSIYA ETILGAN MODUL: (yuqoridagi mavjud modullardan birini tanlang)
                4. SABAB: (nima uchun aynan shu modul)
                5. KEYINGI QADAM: (qanday o'qish kerak)
                
                Javob o'zbek tilida, rag'batlantiruvchi bo'lsin.
                """);

        return callGemini(prompt.toString());
    }

    // ================================================================
    // GEMINI API chaqiruvi
    // ================================================================
    private String callGemini(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String urlWithKey = GEMINI_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    urlWithKey, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content =
                            (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts =
                            (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            return "Xatolik: " + e.getMessage();
        }
        return "";
    }

    // ================================================================
    // AI javobini parse qilish
    // "1:TO'G'RI\n2:NOTO'G'RI" → Map<Long, Boolean>
    // ================================================================
    private Map<Long, Boolean> parseCheckResult(String aiResponse,
                                                Set<Long> questionIds) {
        Map<Long, Boolean> result = new HashMap<>();

        // Default hammasi false
        for (Long id : questionIds) {
            result.put(id, false);
        }

        if (aiResponse == null || aiResponse.isEmpty()) return result;

        String[] lines = aiResponse.trim().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                try {
                    Long qId = Long.parseLong(parts[0].trim());
                    boolean isCorrect = parts[1].trim()
                            .toUpperCase()
                            .contains("TO'G'RI") ||
                            parts[1].trim()
                                    .toUpperCase()
                                    .contains("TOGRI") ||
                            parts[1].trim()
                                    .toUpperCase()
                                    .contains("TRUE") ||
                            parts[1].trim()
                                    .toUpperCase()
                                    .contains("CORRECT");
                    result.put(qId, isCorrect);
                } catch (NumberFormatException ignored) {
                    // parse bo'lmasa o'tkazib yuboramiz
                }
            }
        }
        return result;
    }
}