package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqTestResult;
import sfera.eduportal2.service.TestResultService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test-result")

public class TestResultController {


    private final TestResultService testResultService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getAll() {
        ApiResponse response = testResultService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOne(@PathVariable Long id) {
        ApiResponse response = testResultService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> save(@RequestBody ReqTestResult reqTestResult) {
        ApiResponse response = testResultService.save(reqTestResult);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ReqTestResult reqTestResult) {
        ApiResponse response = testResultService.update(id, reqTestResult);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        ApiResponse response = testResultService.delete(id);
        return ResponseEntity.ok(response);
    }

}
