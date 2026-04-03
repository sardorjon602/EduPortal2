package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqTest;
import sfera.eduportal2.service.TestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")

public class TestController {


    private final TestService testService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getAll() {
        ApiResponse response = testService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOne(@PathVariable Long id) {
        ApiResponse response = testService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> save(@RequestBody ReqTest reqTest) {
        ApiResponse response = testService.save(reqTest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ReqTest reqTest) {
        ApiResponse response = testService.update(id, reqTest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        ApiResponse response = testService.delete(id);
        return ResponseEntity.ok(response);
    }


}
