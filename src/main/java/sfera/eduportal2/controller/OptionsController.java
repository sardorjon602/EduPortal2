package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqOptions;
import sfera.eduportal2.service.OptionsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/options")
public class OptionsController {

    private final OptionsService optionsService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> findAllOptions() {
        ApiResponse response = optionsService.findAll();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id) {
        ApiResponse response = optionsService.findById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteOptions(@PathVariable Long id) {
        ApiResponse response = optionsService.deleteOption(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateOptions(@PathVariable Long id,@RequestBody ReqOptions reqOptions) {
        ApiResponse response = optionsService.updateOption(id, reqOptions);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/save-all/{questionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> saveOptions(@PathVariable Long questionId,@RequestBody List<ReqOptions> reqOptionsList) {
        ApiResponse response = optionsService.saveOptions(questionId, reqOptionsList);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}