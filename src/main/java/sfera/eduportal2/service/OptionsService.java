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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OptionsService {

    private final OptionsRepository optionsRepository;
    private final QuestionsRepository questionsRepository;

    public ApiResponse findAll(){
        List<Options> optionsList = optionsRepository.findAll();
        List<ResOptions> resOptionsList = new ArrayList<>();
        for (Options options : optionsList) {
            ResOptions resOptions = ResOptions.builder()
                    .text(options.getText())
                    .questions(options.getQuestions())
                    .isCorrect(options.isCorrect())
                    .build();
            resOptionsList.add(resOptions);

        }
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resOptionsList)
                .build();
    }

    public ApiResponse findById(Long id) {
        Optional<Options> byText = optionsRepository.findById(id);
        if(byText.isEmpty()){
            return ApiResponse.builder()
                    .message("Option not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }
        Options options = byText.get();
            ResOptions resOptions = ResOptions.builder()
                    .text(options.getText())
                    .isCorrect(options.isCorrect())
                    .questions(options.getQuestions())
                    .build();
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resOptions)
                .build();
    }
    public ApiResponse saveOption(Options options) {
        Options options1 = Options.builder()
                .text(options.getText())
                .isCorrect(options.isCorrect())
                .questions(options.getQuestions())
                .build();
        optionsRepository.save(options1);
        return ApiResponse.builder()
                .message("Option saved successfully!")
                .success(true)
                .status(HttpStatus.OK)
                .body(options1)
                .build();
    }
    public ApiResponse updateOption(Long id, ReqOptions reqOptions) {
        Optional<Options> byId = optionsRepository.findById(id);
        if(byId.isEmpty()){
            return ApiResponse.builder()
                    .message("Option not found!")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .body(null)
                    .build();
        }
        Options options = byId.get();
        options.setText(reqOptions.getText());
        options.setCorrect(reqOptions.isCorrect());
        options.setQuestions(reqOptions.getQuestions());
        optionsRepository.save(options);
        return ApiResponse.builder()
                .message("Option successfully updated")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }
    public ApiResponse deleteOption(Long id) {
        Optional<Options> options = optionsRepository.findById(id);
        if (options.isPresent()){
            Options options1 = options.get();
            return ApiResponse.builder()
                    .message("Option deleted successfully")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(options1)
                    .build();
        }
        return ApiResponse.builder()
                .message("Option not found!")
                .success(false)
                .status(HttpStatus.NOT_FOUND)
                .body(options)
                .build();
    }
}
