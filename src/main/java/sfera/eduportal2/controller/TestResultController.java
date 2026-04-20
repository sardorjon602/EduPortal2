package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Security.CurrentUser;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.service.TestResultService;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService testResultService;

    // 5. username filtr qo'shildi
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "(ADMIN) Barcha natijalar, username bo'yicha filtr")
    public ResponseEntity<ApiResponse> getAllResults(@RequestParam(required = false)   String username ) {
        ApiResponse response = testResultService.getAllResults(username);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // 6. userId non-required — admin beradi, user tokendan olinadi
    @GetMapping("/my-results")
    @Operation(summary = "Test natijalari — user tokendan, admin userId beradi")
    public ResponseEntity<ApiResponse> getMyResults(
            @CurrentUser Users currentUser,
            @RequestParam(required = false) Long userId) {
        ApiResponse response = testResultService.getMyResults(currentUser, userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID orqali bitta natijani ko'rish")
    public ResponseEntity<ApiResponse> getResultById(@PathVariable Long id) {
        ApiResponse response = testResultService.getResultById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}