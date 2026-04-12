package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.service.TestService;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse> startTest(@RequestBody ReqStartTest req) {
        ApiResponse response = testService.startTest(req);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<ApiResponse> stopTest(@RequestBody ReqStopTest req) {
        ApiResponse response = testService.stopTest(req);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}