package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.response.ResTestResult;
import sfera.eduportal2.Repository.TestResultRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.entity.TestResult;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;

    // ================================================================
    // 5. ADMIN: barcha natijalar + username filtr
    // ================================================================
//    public ApiResponse getAllResults(String username) {
//        List<TestResult> results = testResultRepository
//                .findAllByUsernameFilter(username);
//
//        List<ResTestResult> responseList = results.stream()
//                .map(this::toResponseDTO)
//                .collect(Collectors.toList());
//
//        return ApiResponse.builder()
//                .message("Barcha test natijalari olindi")
//                .success(true)
//                .status(HttpStatus.OK)
//                .body(responseList)
//                .build();
//    }


    // ================================================================
    // 5. ADMIN: barcha natijalar (Filtrsiz)
    // ================================================================
    public ApiResponse getAllResults() {
        // findAllByUsernameFilter o'rniga barchasini oladigan findAll() ishlatiladi
        List<TestResult> results = testResultRepository.findAll();

        List<ResTestResult> responseList = results.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return ApiResponse.builder()
                .message("Barcha test natijalari olindi")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseList)
                .build();
    }

    // ================================================================
    // 6. getMyResults — admin userId beradi, user tokendan olinadi
    // ================================================================
    public ApiResponse getMyResults(Users currentUser, Long userId) {
        Users targetUser;

        if (currentUser.getRole().getRole().equals(Role.ROLE_ADMIN) && userId != null) {
            // Admin boshqa userning natijalarini ko'rmoqda
            targetUser = userRepository.findById(userId).orElse(null);
            if (targetUser == null) {
                return ApiResponse.builder()
                        .message("Foydalanuvchi topilmadi")
                        .success(false)
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
        } else {
            // Oddiy user o'z natijalarini ko'rmoqda
            targetUser = currentUser;
        }

        List<TestResult> userResults = testResultRepository
                .findByUsersOrderByCreatedAtDesc(targetUser);

        if (userResults.isEmpty()) {
            return ApiResponse.builder()
                    .message("Hali test natijalari yo'q")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(List.of())
                    .build();
        }

        List<ResTestResult> responseList = userResults.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return ApiResponse.builder()
                .message("Test natijalari")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseList)
                .build();
    }

    public ApiResponse getResultById(Long id) {
        Optional<TestResult> resultOpt = testResultRepository.findById(id);
        if (resultOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test natijasi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        return ApiResponse.builder()
                .message("Test natijasi topildi")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResponseDTO(resultOpt.get()))
                .build();
    }

    private ResTestResult toResponseDTO(TestResult result) {
        String aiRecommendation = result.getAiRecommendation() != null
                ? result.getAiRecommendation() : "AI tavsiyasi mavjud emas";

        String categoryName = "Noma'lum";
        if (result.getTestSession() != null
                && result.getTestSession().getCategory() != null) {
            categoryName = result.getTestSession().getCategory().getName();
        }

        LocalDateTime takenAt = result.getCreatedAt();

        return ResTestResult.builder()
                .id(result.getId())
                .userId(result.getUsers().getId())
                .userName(result.getUsers().getFullName())
                .categoryName(categoryName)
                .correctCount(result.getCorrectCount())
                .totalCount(result.getTotalCount())
                .scorePercent(result.getScorePercent())
                .recommendedModule(result.getRecommendedModule() != null
                        ? result.getRecommendedModule() : "Aniqlanmadi")
                .aiRecommendation(aiRecommendation)
                .takenAt(takenAt)
                .build();
    }
}