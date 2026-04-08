package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqQuestions;
import sfera.eduportal2.service.QuestionsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionsController {

    private final QuestionsService questionsService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveQuestions(@RequestBody ReqQuestions reqQuestions) {
        ApiResponse response = questionsService.saveQuestion(reqQuestions);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateQuestion(@PathVariable Long id,
                                                      @RequestBody ReqQuestions reqQuestions) {
        ApiResponse response = questionsService.updateQuestion(id, reqQuestions);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteQuestion(@PathVariable Long id) {
        ApiResponse response = questionsService.deleteQuestion(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> findAllQuestions() {
        ApiResponse response = questionsService.findAll();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id) {
        ApiResponse response = questionsService.findById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}