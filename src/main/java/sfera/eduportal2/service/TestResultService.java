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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;





    // ====================================================================
    // ADMIN UCHUN: Barcha natijalarni ko'rish
    // ====================================================================
    public ApiResponse getAllResults() {
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

    // ====================================================================
    // O'QUVCHI UCHUN: Faqat o'zining natijalarini ko'rish
    // ====================================================================
    public ApiResponse getMyResults(Long userId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Foydalanuvchi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<TestResult> userResults = testResultRepository
                .findByUsersOrderByCreatedAtDesc(userOpt.get());

        if (userResults.isEmpty()) {
            return ApiResponse.builder()
                    .message("Sizda hali test natijalari yo'q")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(List.of())
                    .build();
        }

        List<ResTestResult> responseList = userResults.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return ApiResponse.builder()
                .message("Sizning test natijalaringiz")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseList)
                .build();
    }

    // ====================================================================
    // ID orqali bitta natijani to'liq ko'rish
    // ====================================================================
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

    // ==================== HELPER METHOD ====================
    private ResTestResult toResponseDTO(TestResult result) {

        // AI tavsiyasi
        String aiRecommendation = result.getAiRecommendation() != null
                ? result.getAiRecommendation()
                : "AI tavsiyasi mavjud emas";

        // Category nomi
        String categoryName = "Noma'lum";
        if (result.getTestSession() != null && result.getTestSession().getCategory() != null) {
            categoryName = result.getTestSession().getCategory().getName();
        }

        // takenAt ni olish (entitydagi takenAt ustunligi beriladi)
        LocalDateTime takenAt = result.getTakenAt() != null
                ? result.getTakenAt()
                : (result.getTestSession() != null
                ? result.getTestSession().getEndTime()
                : result.getCreatedAt());

        return ResTestResult.builder()
                .id(result.getId())
                .userId(result.getUsers().getId())
                .userName(result.getUsers().getFullName())
                .categoryName(categoryName)

                .correctCount(result.getCorrectCount())
                .totalCount(result.getTotalCount())
                .scorePercent(result.getScorePercent())

                .recommendedModule(result.getRecommendedModule() != null
                        ? result.getRecommendedModule()
                        : "Aniqlanmadi")
                .aiRecommendation(aiRecommendation)
                .takenAt(takenAt)
                .build();
    }
}