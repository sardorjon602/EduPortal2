package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.request.ReqRecommendation;
import sfera.eduportal2.Payload.response.ResRecommendation;
import sfera.eduportal2.service.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendation")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI va qo'lda modul tavsiyalarini boshqarish")
public class RecommendationController {

    private final RecommendationService recommendationService;

    // ==================== AI: AVTOMATIK TAVSIYA ====================

    @Operation(
            summary = "AI orqali avtomatik tavsiya yaratish",
            description = """
                    Foydalanuvchining test natijalari va darajasiga qarab,
                    Gemini AI platformadagi modullardan eng mositini tanlaydi va
                    bazaga saqlaydi. ADMIN va USER uchun.
                    """
    )
    @PostMapping("/ai/generate/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResRecommendation> generateByAi(@PathVariable Long userId) {
        ResRecommendation response = recommendationService.generateAndSave(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== QOLDA TAVSIYA YARATISH (ADMIN) ====================

    @Operation(
            summary = "Qo'lda tavsiya yaratish",
            description = "Admin modul nomini va sababini o'zi kiritib tavsiya qo'shadi. Faqat ADMIN uchun."
    )
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResRecommendation> create(@RequestBody ReqRecommendation req) {
        ResRecommendation response = recommendationService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== GET ALL ====================

    @Operation(
            summary = "Barcha tavsiyalarni olish",
            description = "Tizimda mavjud barcha tavsiyalar ro'yxatini qaytaradi. Faqat ADMIN uchun."
    )
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ResRecommendation>> getAll() {
        return ResponseEntity.ok(recommendationService.getAll());
    }

    // ==================== GET BY ID ====================

    @Operation(
            summary = "ID bo'yicha tavsiyani olish",
            description = "Berilgan ID ga mos tavsiyani qaytaradi. ADMIN va USER uchun."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResRecommendation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getById(id));
    }

    // ==================== GET BY USER ====================

    @Operation(
            summary = "Foydalanuvchi bo'yicha tavsiyalarni olish",
            description = "Berilgan userId ga tegishli barcha tavsiyalarni qaytaradi. ADMIN va USER uchun."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<ResRecommendation>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(recommendationService.getByUserId(userId));
    }

    // ==================== UPDATE ====================

    @Operation(
            summary = "Tavsiyani yangilash",
            description = "Mavjud tavsiyani yangi ma'lumotlar bilan yangilaydi. Faqat ADMIN uchun."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResRecommendation> update(
            @PathVariable Long id,
            @RequestBody ReqRecommendation req
    ) {
        return ResponseEntity.ok(recommendationService.update(id, req));
    }

    // ==================== DELETE ====================

    @Operation(
            summary = "Tavsiyani o'chirish",
            description = "Berilgan ID li tavsiyani o'chiradi. Faqat ADMIN uchun."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        recommendationService.delete(id);
        return ResponseEntity.ok("Tavsiya muvaffaqiyatli o'chirildi.");
    }
}