package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqOptions;
import sfera.eduportal2.entity.Options;
import sfera.eduportal2.service.OptionsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/options")
public class OptionsController {
    private final OptionsService optionsService;

    @GetMapping("/List")
    public ResponseEntity<ApiResponse> findAllOptions() {
        ApiResponse response = optionsService.findAll();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse> findById(@RequestBody ReqOptions reqOptions){
        ApiResponse response = optionsService.findById(reqOptions.getId());
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse> deleteOptions(@PathVariable Long id){
        ApiResponse response = optionsService.deleteOption(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PutMapping("{id}")
    public ResponseEntity<ApiResponse> updateOptions(@PathVariable Long id, @RequestBody ReqOptions reqOptions){
        ApiResponse response = optionsService.updateOption(id,reqOptions);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveOptions(@RequestBody Options  options) {
        ApiResponse response = optionsService.saveOption(options);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
