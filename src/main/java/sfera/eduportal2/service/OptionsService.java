package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqOptions;
import sfera.eduportal2.Payload.response.ResOptions;
import sfera.eduportal2.Repository.OptionsRepository;
import sfera.eduportal2.Repository.QuestionsRepository;
import sfera.eduportal2.entity.Options;
import sfera.eduportal2.entity.Questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OptionsService {

    private final OptionsRepository optionsRepository;
    private final QuestionsRepository questionsRepository;

    public ApiResponse findAll() {
        List<Options> optionsList = optionsRepository.findAll();
        List<ResOptions> resOptionsList = new ArrayList<>();
        for (Options options : optionsList) {
            resOptionsList.add(toResOptions(options));
        }
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resOptionsList)
                .build();
    }

    public ApiResponse findById(Long id) {
        Optional<Options> byId = optionsRepository.findById(id);
        if (byId.isEmpty()) {
            return ApiResponse.builder()
                    .message("Option not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResOptions(byId.get()))
                .build();
    }

    public ApiResponse saveOption(ReqOptions reqOptions) {
        Optional<Questions> questionOpt = questionsRepository.findById(reqOptions.getQuestionId());
        if (questionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Question not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        Options options = Options.builder()
                .text(reqOptions.getText())
                .isCorrect(reqOptions.isCorrect())
                .questions(questionOpt.get())
                .build();

        optionsRepository.save(options);

        return ApiResponse.builder()
                .message("Option saved successfully!")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResOptions(options))
                .build();
    }

    public ApiResponse updateOption(Long id, ReqOptions reqOptions) {
        Optional<Options> byId = optionsRepository.findById(id);
        if (byId.isEmpty()) {
            return ApiResponse.builder()
                    .message("Option not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        Optional<Questions> questions = questionsRepository.findById(reqOptions.getQuestionId());
        if (questions.isEmpty()) {
            return ApiResponse.builder()
                    .message("Question not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        Options options = byId.get();
        options.setText(reqOptions.getText());
        options.setCorrect(reqOptions.isCorrect());
        options.setQuestions(questions.get());

        optionsRepository.save(options);

        return ApiResponse.builder()
                .message("Option successfully updated")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResOptions(options))
                .build();
    }

    public ApiResponse deleteOption(Long id) {
        Optional<Options> byId = optionsRepository.findById(id);
        if (byId.isPresent()) {
            optionsRepository.delete(byId.get());
            return ApiResponse.builder()
                    .message("Option deleted successfully")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(null)
                    .build();
        }
        return ApiResponse.builder()
                .message("Option not found!")
                .success(false)
                .status(HttpStatus.NOT_FOUND)
                .body(null)
                .build();
    }

    private ResOptions toResOptions(Options options) {
        return ResOptions.builder()
                .text(options.getText())
                .isCorrect(options.isCorrect())
                .questionId(options.getQuestions().getId())
                .questionText(options.getQuestions().getText())
                .build();
    }
}