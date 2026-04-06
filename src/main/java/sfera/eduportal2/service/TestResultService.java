package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqTestResult;
import sfera.eduportal2.Payload.response.ResTestResult;
import sfera.eduportal2.Repository.TestRepository;
import sfera.eduportal2.Repository.TestResultRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.entity.Test;
import sfera.eduportal2.entity.TestResult;
import sfera.eduportal2.entity.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final UserRepository usersRepository;
    private final TestRepository testRepository;

    public ApiResponse findAll() {
        List<TestResult> all = testResultRepository.findAll();
        List<ResTestResult> resTestResults = new ArrayList<>();
        for (TestResult result : all) {
            ResTestResult res = ResTestResult.builder()
                    .id(result.getId())
                    .userName(result.getUsers().getFullName())
                    .testName(result.getTest().getTitle())
                    .score(result.getScore())
                    .takenAt(result.getTakenAt())
                    .build();
            resTestResults.add(res);
        }
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resTestResults)
                .build();
    }

    public ApiResponse findById(Long id) {
        Optional<TestResult> optional = testResultRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("TestResult not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        TestResult result = optional.get();
        ResTestResult res = ResTestResult.builder()
                .id(result.getId())
                .userName(result.getUsers().getFullName())
                .testName(result.getTest().getTitle())
                .score(result.getScore())
                .takenAt(result.getTakenAt())
                .build();
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(res)
                .build();
    }

    public ApiResponse save(ReqTestResult reqTestResult) {
        Optional<Users> userOptional = usersRepository.findById(reqTestResult.getUserId());
        if (userOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("User not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Test> testOptional = testRepository.findById(reqTestResult.getTestId());
        if (testOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestResult testResult = TestResult.builder()
                .users(userOptional.get())
                .test(testOptional.get())
                .score(reqTestResult.getScore())
                .takenAt(reqTestResult.getTakenAt())
                .build();
        testResultRepository.save(testResult);

        return ApiResponse.builder()
                .message("TestResult successfully saved")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse update(Long id, ReqTestResult reqTestResult) {
        Optional<TestResult> optional = testResultRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("TestResult not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Users> userOptional = usersRepository.findById(reqTestResult.getUserId());
        if (userOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("User not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Test> testOptional = testRepository.findById(reqTestResult.getTestId());
        if (testOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestResult toUpdate = optional.get();
        toUpdate.setUsers(userOptional.get());
        toUpdate.setTest(testOptional.get());
        toUpdate.setScore(reqTestResult.getScore());
        toUpdate.setTakenAt(reqTestResult.getTakenAt());
        testResultRepository.save(toUpdate);

        return ApiResponse.builder()
                .message("TestResult successfully updated")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse delete(Long id) {
        Optional<TestResult> optional = testResultRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("TestResult not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        testResultRepository.delete(optional.get());
        return ApiResponse.builder()
                .message("TestResult successfully deleted")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }


}
