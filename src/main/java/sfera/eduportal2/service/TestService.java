package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqTest;
import sfera.eduportal2.Payload.response.ResTest;
import sfera.eduportal2.Repository.ModuleRepository;
import sfera.eduportal2.Repository.OptionsRepository;
import sfera.eduportal2.Repository.TestRepository;
import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.Options;
import sfera.eduportal2.entity.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class TestService {


    private final TestRepository testRepository;
    private final ModuleRepository moduleRepository;
    private final OptionsRepository optionsRepository;

    public ApiResponse findAll() {
        List<Test> all = testRepository.findAll();
        List<ResTest> resTests = new ArrayList<>();
        for (Test test : all) {
            ResTest resTest = ResTest.builder()
                    .id(test.getId())
                    .title(test.getTitle())
                    .moduleName(test.getModule().getModuleName())
                    .difficulty(test.getDifficulty())
                    .optionName(test.getOptions().getText())
                    .timeLimit(test.getTimeLimit())
                    .build();
            resTests.add(resTest);
        }
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resTests)
                .build();
    }

    public ApiResponse findById(Long id) {
        Optional<Test> optional = testRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        Test test = optional.get();
        ResTest resTest = ResTest.builder()
                .id(test.getId())
                .title(test.getTitle())
                .moduleName(test.getModule().getModuleName())
                .difficulty(test.getDifficulty())
                .optionName(test.getOptions().getText())
                .timeLimit(test.getTimeLimit())
                .build();
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resTest)
                .build();
    }

    public ApiResponse save(ReqTest reqTest) {
        boolean exists = testRepository.existsByTitleIgnoreCase(reqTest.getTitle());
        if (exists) {
            return ApiResponse.builder()
                    .message("Test already exists")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<sfera.eduportal2.entity.Module> moduleOptional = moduleRepository.findById(reqTest.getModuleId());
        if (moduleOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Options> optionsOptional = optionsRepository.findById(reqTest.getOptionId());
        if (optionsOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Option not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Test test = Test.builder()
                .title(reqTest.getTitle())
                .module(moduleOptional.get())
                .difficulty(reqTest.getDifficulty())
                .options(optionsOptional.get())
                .timeLimit(reqTest.getTimeLimit())
                .build();
        testRepository.save(test);

        return ApiResponse.builder()
                .message("Test successfully saved")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse update(Long id, ReqTest reqTest) {
        Optional<Test> optional = testRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        boolean titleExists = testRepository.existsByTitleIgnoreCaseAndIdNot(reqTest.getTitle(), id);
        if (titleExists) {
            return ApiResponse.builder()
                    .message("Test with this title already exists")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<Module> moduleOptional = moduleRepository.findById(reqTest.getModuleId());
        if (moduleOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Options> optionsOptional = optionsRepository.findById(reqTest.getOptionId());
        if (optionsOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Option not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Test toUpdate = optional.get();
        toUpdate.setTitle(reqTest.getTitle());
        toUpdate.setModule(moduleOptional.get());
        toUpdate.setDifficulty(reqTest.getDifficulty());
        toUpdate.setOptions(optionsOptional.get());
        toUpdate.setTimeLimit(reqTest.getTimeLimit());
        testRepository.save(toUpdate);

        return ApiResponse.builder()
                .message("Test successfully updated")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse delete(Long id) {
        Optional<Test> optional = testRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        testRepository.delete(optional.get());
        return ApiResponse.builder()
                .message("Test successfully deleted")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }


}
