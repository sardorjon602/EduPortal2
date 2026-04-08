package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.response.ResAiRecommendation;
import sfera.eduportal2.service.EduPortalAiService;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Recommendation", description = "Sun'iy intellekt asosidagi tavsiya va tahlil xizmatlari")
public class AiRecommendationController {

    private final EduPortalAiService aiService;

    /**
     * O'quvchining test natijalari va darajasiga qarab
     * platformadagi mos kurslarni tavsiya qiladi.
     * Faqat autentifikatsiya qilingan foydalanuvchilar uchun.
     */
    @Operation(summary = "Kurs tavsiyasi",
               description = "Foydalanuvchi ID si bo'yicha AI kurs tavsiyasini qaytaradi")
    @GetMapping("/recommend/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResAiRecommendation> recommendCourses(@PathVariable Long userId) {
        ResAiRecommendation result = aiService.recommendCoursesForUser(userId);
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Berilgan test natijasini AI orqali batafsil tahlil qiladi.
     */
    @Operation(summary = "Test natijasini tahlil qilish",
               description = "TestResult ID si bo'yicha AI batafsil tahlil va maslahat beradi")
    @GetMapping("/analyze-result/{testResultId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResAiRecommendation> analyzeTestResult(@PathVariable Long testResultId) {
        ResAiRecommendation result = aiService.analyzeTestResult(testResultId);
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * O'quvchining maqsadi va qiziqishlariga qarab
     * shaxsiy o'qish yo'l xaritasini tuzadi.
     */
    @Operation(summary = "Shaxsiy o'qish yo'l xaritasi",
               description = "Maqsad va qiziqishlarga asoslangan 3 bosqichli o'quv rejasi")
    @PostMapping("/learning-path/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResAiRecommendation> generateLearningPath(
            @PathVariable Long userId,
            @RequestBody LearningPathRequest request) {
        ResAiRecommendation result = aiService.generateLearningPath(
                userId,
                request.goal(),
                request.interests()
        );
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // Request DTO (record — oddiy va toza)
    public record LearningPathRequest(String goal, String interests) {}
}