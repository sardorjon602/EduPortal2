package sfera.eduportal2.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqQuestions;
import sfera.eduportal2.entity.Questions;
import sfera.eduportal2.service.QuestionsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionsController {
    private final QuestionsService questionsService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveQuestions(@RequestBody Questions questions){
        ApiResponse response = questionsService.saveQuestion(questions);
        return  ResponseEntity.status(response.getStatus()).body(response);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse> updateQuestion(@PathVariable Long id, @RequestBody ReqQuestions reqQuestions) {
//        ApiResponse response = questionsService.updateQuestion(id,reqQuestions);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse> deleteQuestion(@PathVariable Long id){
        ApiResponse response = questionsService.deleteQuestion(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> findAllQuestions(){
        ApiResponse response = questionsService.findAll();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse> findById(@RequestBody ReqQuestions reqQuestions){
        ApiResponse response = questionsService.findById((reqQuestions.getId()));
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
