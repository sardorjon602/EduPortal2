package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Exception.NotFoundException;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqQuestions;
import sfera.eduportal2.Payload.response.ResQuestions;
import sfera.eduportal2.Repository.ModuleRepository;
import sfera.eduportal2.Repository.QuestionsRepository;
import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.Questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionsService {

    private final QuestionsRepository questionsRepository;
    private final ModuleRepository moduleRepository;

    public ApiResponse saveQuestion(ReqQuestions reqQuestions) {
        Optional<Module> module = moduleRepository.findById(reqQuestions.getModuleId());
        if (module.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        Questions question = Questions.builder()
                .text(reqQuestions.getText())
                .module(module.get())
                .type(reqQuestions.getType())
                .build();


        questionsRepository.save(question);

        return ApiResponse.builder()
                .message("Saved successfully")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResQuestions(question))
                .build();
    }

    public ApiResponse updateQuestion(Long id, ReqQuestions reqQuestions) {
        Optional<Questions> byId = questionsRepository.findById(id);
        if (byId.isEmpty()) {
            return ApiResponse.builder()
                    .message("Question not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        Optional<Module> module = moduleRepository.findById(reqQuestions.getModuleId());
        if (module.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }
        Questions questions = byId.get();
        questions.setText(reqQuestions.getText());
        questions.setType(reqQuestions.getType());
        questions.setModule(module.get());

        questionsRepository.save(questions);

        return ApiResponse.builder()
                .message("Question updated successfully!")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResQuestions(questions))
                .build();
    }

    public ApiResponse findById(Long id) {
        Optional<Questions> byId = questionsRepository.findById(id);
        if (byId.isEmpty()) {
            return ApiResponse.builder()
                    .message("Question not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResQuestions(byId.get()))
                .build();
    }

    public ApiResponse findAll() {
        List<Questions> questionsList = questionsRepository.findAll();
        List<ResQuestions> resQuestionsList = new ArrayList<>();
        for (Questions questions : questionsList) {
            resQuestionsList.add(toResQuestions(questions));
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

//        Optional<Questions> questions = questionsRepository.findById(id).orElseThrow(
//                () -> new NotFoundException("Savol topilmadi")
//        );

        if (questions.isPresent()) {
            questionsRepository.delete(questions.get());
            return ApiResponse.builder()
                    .message("Question deleted successfully")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(null)
                    .build();
        } else {
            return ApiResponse.builder()
                    .message("Question not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }
    }

    private ResQuestions toResQuestions(Questions questions) {
        return ResQuestions.builder()
                .id(questions.getId())
                .text(questions.getText())
                .type(questions.getType())
                .moduleId(questions.getModule().getId())
                .moduleName(questions.getModule().getModuleName())
                .build();
    }
}