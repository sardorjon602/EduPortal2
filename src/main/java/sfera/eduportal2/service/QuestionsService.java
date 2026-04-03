package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqQuestions;
import sfera.eduportal2.Payload.request.ReqUser;
import sfera.eduportal2.Payload.response.ResQuestions;
import sfera.eduportal2.Repository.QuestionsRepository;
import sfera.eduportal2.entity.Questions;
import sfera.eduportal2.entity.enums.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionsService {
    private final QuestionsRepository questionsRepository;
//    private final TypeRepository typeRepository;
    public ApiResponse saveQuestion(Questions questions) {
            Questions questions1 = Questions.builder()
                    .text(questions.getText())
                    .module(questions.getModule())
                    .questionCount(questions.getQuestionCount())
                    .type(questions.getType())
                    .build();
            questionsRepository.save(questions1);
            return ApiResponse.builder()
                    .message("Question saved successfully!")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(questions1)
                    .build();
        }
//        public ApiResponse updateQuestion(Long id, ReqQuestions reqQuestions) {
//                Optional<Questions> byId = questionsRepository.findById(id);
//                if(byId.isEmpty()) {
//                    return ApiResponse.builder()
//                            .message("Question not found!")
//                            .success(false)
//                            .status(HttpStatus.NOT_FOUND)
//                            .body(null)
//                            .build();
//                }
//               Questions questions1 = Questions.builder()
//                       .text(reqQuestions.getText())
//                       .questionCount(Integer.valueOf(reqQuestions.getText()))
//                       .build();
//                questionsRepository.save(questions1);
//                return ApiResponse.builder()
//                        .message("Question updated successfully!")
//                        .success(true)
//                        .status(HttpStatus.OK)
//                        .body(questions1)
//                        .build();
//
//            }
            public ApiResponse findById(Long id) {
                Optional<Questions> byId = questionsRepository.findById(id);
                if(byId.isEmpty()){
                    return ApiResponse.builder()
                            .message("Question not found!")
                            .success(false)
                            .status(HttpStatus.NOT_FOUND)
                            .body(null)
                            .build();
                }
                Questions questions = byId.get();
                ResQuestions resQuestions = ResQuestions.builder()
                        .text(questions.getText())
                        .type(questions.getType())
                        .module(questions.getModule())
                        .questionCount(questions.getQuestionCount())
                        .build();
                return ApiResponse.builder()
                        .message("Success")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(resQuestions)
                        .build();
            }

            public ApiResponse findAll() {
                List<Questions> questionsList = questionsRepository.findAll();
                List<ResQuestions> resQuestionsList = new ArrayList<>();
                for (Questions questions : questionsList) {
                    ResQuestions resQuestions = ResQuestions.builder()
                            .text(questions.getText())
                            .module(questions.getModule())
                            .questionCount(questions.getQuestionCount())
                            .type(questions.getType())
                            .build();
                    resQuestionsList.add(resQuestions);
                }
                return ApiResponse.builder()
                        .message("Success!")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(resQuestionsList)
                        .build();
            }
            public ApiResponse deleteQuestion(Long id) {
                Optional<Questions> questions = questionsRepository.findById(id);
                if(questions.isPresent()){
                    Questions questions1 = questions.get();
                    questionsRepository.delete(questions1);
                    return ApiResponse.builder()
                            .message("Question deleted successfully")
                            .success(true)
                            .status(HttpStatus.OK)
                            .body(questions)
                            .build();
                }else {
                    return ApiResponse.builder()
                            .message("Question not found")
                            .success(false)
                            .status(HttpStatus.NOT_FOUND)
                            .body(questions)
                            .build();
                }
        }
}


