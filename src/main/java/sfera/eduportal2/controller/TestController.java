package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.service.TestService;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/start")
    @Operation(summary = "Modul bo'yicha testni boshlash")
    // @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_USER')")
    public ResponseEntity<ApiResponse> startTest(@RequestBody ReqStartTest request) {
        ApiResponse response = testService.startTest(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/stop")
    @Operation(summary = "Testni yakunlash va AI dan tavsiya olish")
    // @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_USER')")
    public ResponseEntity<ApiResponse> stopTest(@RequestBody ReqStopTest request) {
        ApiResponse response = testService.stopTest(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}