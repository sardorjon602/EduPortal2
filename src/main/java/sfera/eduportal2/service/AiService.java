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

    public String getRecommendation(Long userId, List<String> wrongAnswers, double score) {
        // Barcha modullar ro'yxati
        List<String> allModules = moduleRepository.findAll()
                .stream()
                .map(m -> m.getModuleName())
                .toList();

        String prompt = buildPrompt(wrongAnswers, score, allModules);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-sonnet-4-20250514");
        body.put("max_tokens", 1000);
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages",
                entity,
                Map.class
        );

        // Javobni parse qilish
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        return content.get(0).get("text").toString();
    }

    private String buildPrompt(List<String> wrongAnswers, double score, List<String> modules) {
        StringBuilder sb = new StringBuilder();
        sb.append("Siz ta'lim maslahatchiсиз. Foydalanuvchi test topshirdi.\n\n");
        sb.append("Natija: ").append(String.format("%.1f", score)).append("%\n\n");

        if (!wrongAnswers.isEmpty()) {
            sb.append("Noto'g'ri javob berilgan savollar va ularning modullari:\n");
            for (String w : wrongAnswers) {
                sb.append("- ").append(w).append("\n");
            }
        }

        sb.append("\nMavjud modullar ro'yxati:\n");
        for (String m : modules) {
            sb.append("- ").append(m).append("\n");
        }

        sb.append("""
            \nIltimos, quyidagilarni bajaring:
            1. Foydalanuvchining zaif tomonlarini tahlil qiling
            2. Qaysi modulni o'rganishni tavsiya qilasiz va NIMA UCHUN - batafsil tushuntiring
            3. Eng oxirgi qatorda FAQAT quyidagi formatda yozing:
            TAVSIYA MODUL: <modul nomi>
            
            Javobni o'zbek tilida bering.
            """);

        return sb.toString();
    }
}