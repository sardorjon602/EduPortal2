package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Agar Spring Security ulangan bo'lsa
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.Payload.request.ReqTest;
import sfera.eduportal2.service.TestService;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // ----- ADMIN ENDPOINTLARI -----

     @PreAuthorize("hasRole('ROLE_ADMIN')") // Faqat admin kiroladi
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createTest(@RequestBody ReqTest reqTest) {
        ApiResponse response = testService.createTest(reqTest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteTest(@PathVariable Long id) {
        ApiResponse response = testService.deleteTest(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

     @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateTest(@PathVariable Long id, @RequestBody ReqTest reqTest) {
         ApiResponse response = testService.updateTest(id, reqTest);
         return ResponseEntity.status(response.getStatus()).body(response);
    }


    // ----- O'QUVCHI (USER) ENDPOINTLARI -----

    @PostMapping("/start")
    public ResponseEntity<ApiResponse> startTest(@RequestBody ReqStartTest request) {
        ApiResponse response = testService.startTest(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<ApiResponse> stopTest(@RequestBody ReqStopTest request) {
        ApiResponse response = testService.stopTest(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}