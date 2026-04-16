package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.service.TestResultService;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService testResultService;

    @GetMapping("/all")
    @Operation(summary = "(ADMIN) Barcha o'quvchilarning test natijalarini ko'rish")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> getAllResults() {
        ApiResponse response = testResultService.getAllResults();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/my-results/{userId}")
       @Operation(summary = "(STUDENT) O'zining test natijalari tarixini ko'rish")
    // Agar @CurrentUser ishlatsangiz {userId} ni url dan emas, token ichidan olishingiz ham mumkin.
    // @PreAuthorize("hasAnyRole('USER', 'STUDENT')") 
    public ResponseEntity<ApiResponse> getMyResults(@PathVariable Long userId) {
        ApiResponse response = testResultService.getMyResults(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID orqali bitta natijani ko'rish")
    public ResponseEntity<ApiResponse> getResultById(@PathVariable Long id) {
        ApiResponse response = testResultService.getResultById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}