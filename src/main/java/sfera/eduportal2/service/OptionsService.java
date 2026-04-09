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
import sfera.eduportal2.entity.enums.Type;

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

    public ApiResponse saveOptions(Long questionId, List<ReqOptions> reqOptionsList) {
        Optional<Questions> questionOpt = questionsRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Question not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }

        if (questionOpt.get().getType() != Type.OPTION) {
            return ApiResponse.builder()
                    .message("Options can only be added to OPTION type questions!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null)
                    .build();
        }

        long trueCount = reqOptionsList.stream()
                .filter(ReqOptions::isCorrect)
                .count();

        if (trueCount != 1) {
            return ApiResponse.builder()
                    .message("Exactly one option must be correct!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        boolean alreadyHasCorrect = optionsRepository.existsByQuestionsIdAndIsCorrectTrue(questionId);
        if (alreadyHasCorrect) {
            return ApiResponse.builder()
                    .message("This question already has a correct option!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null)
                    .build();
        }

        List<Options> optionsList = new ArrayList<>();
        for (ReqOptions reqOptions : reqOptionsList) {
            Options options = Options.builder()
                    .text(reqOptions.getText())
                    .isCorrect(reqOptions.isCorrect())
                    .questions(questionOpt.get())
                    .build();
            optionsList.add(options);
        }

        List<Options> savedOptions = optionsRepository.saveAll(optionsList);

        List<ResOptions> resOptionsList = savedOptions.stream()
                .map(this::toResOptions)
                .toList();

        return ApiResponse.builder()
                .message("Options saved successfully!")
                .success(true)
                .status(HttpStatus.CREATED)
                .body(resOptionsList)
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

        if (reqOptions.isCorrect()) {
            boolean anotherCorrectExists = optionsRepository
                    .existsByQuestionsIdAndIsCorrectTrueAndIdNot(
                            reqOptions.getQuestionId(), id
                    );
            if (anotherCorrectExists) {
                return ApiResponse.builder()
                        .message("This question already has a correct option!")
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST)
                        .body(null)
                        .build();
            }
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
                .id(options.getId())
                .text(options.getText())
                .isCorrect(options.isCorrect())
                .questionId(options.getQuestions().getId())
                .questionText(options.getQuestions().getText())
                .build();
    }
}