package sfera.eduportal2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EduPortalAiService {

    // application.properties faylidan API kalitni oladi
    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // Constructor injection (Spring Boot da eng to'g'ri usul)
    public EduPortalAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * O'quvchi ma'lumotlariga asoslanib kurs tavsiya qiluvchi metod.
     * * @param studentName O'quvchining ismi
     * @param testScore O'quvchining baholash testidan olgan bali (masalan, 100 dan)
     * @param interests O'quvchi qiziqqan texnologiyalar (masalan, "Java, Spring Boot, Go")
     * @return AI ning tavsiyasi (JSON formatda)
     */
    public String getCourseRecommendation(String studentName, int testScore, String interests) {
        
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // Prompt Engineering: AI ga aynan nima qilish kerakligini tushuntiramiz
        String promptText = String.format(
            "Sen 'EduPortal' onlayn o'quv markazining sun'iy intellekt maslahatchisisan. " +
            "O'quvchi %s qabul testidan %d/100 ball to'pladi. " +
            "Uning qiziqishlari: %s. " +
            "Ushbu natijalarga asoslanib, uning hozirgi bilim darajasini qisqacha bahola va " +
            "EduPortal platformasidagi qaysi yo'nalishdagi kurslarni o'qishni tavsiya qilasan? " +
            "Javobingni motivatsion ruhda, aniq va faqat o'zbek tilida ber.",
            studentName, testScore, interests
        );

        // Qo'shtirnoqlarni xavfsiz holatga keltirish (JSON buzilmasligi uchun)
        String safePrompt = promptText.replace("\"", "\\\"");

        String requestBody = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\":[{\"text\": \"" + safePrompt + "\"}]\n" +
                "  }]\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody(); 
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Kechirasiz, sun'iy intellekt xizmatida nosozlik yuz berdi.\"}";
        }
    }
}