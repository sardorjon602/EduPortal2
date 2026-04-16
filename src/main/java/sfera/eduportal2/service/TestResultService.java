package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.response.ResTestResult;
import sfera.eduportal2.Repository.RecommendationRepository;
import sfera.eduportal2.Repository.TestResultRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.entity.Recommendation;
import sfera.eduportal2.entity.TestResult;
import sfera.eduportal2.entity.Users;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;

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

        // testResultRepository ichida findByUsersOrderByTakenAtDesc(Users user) metodi bo'lishi kerak
        List<TestResult> userResults = testResultRepository.findByUsersOrderByCreatedAtDesc(userOpt.get());

        if (userResults.isEmpty()) {
             return ApiResponse.builder()
                    .message("Sizda hali test natijalari yo'q")
                    .success(true)
                    .status(HttpStatus.OK)
                    .body(List.of()) // Bo'sh ro'yxat
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
    // ID orqali bitta natijani to'liq ko'rish (Batafsil ma'lumot)
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
    // Entityni DTO ga o'girish
    private ResTestResult toResponseDTO(TestResult result) {
        
        // O'sha natija foydalanuvchisiga AI qanday tavsiya berganini tortib olamiz
        String aiMessage = "Tavsiya topilmadi";
        List<Recommendation> recommendations = recommendationRepository.findAllByUsers(result.getUsers());
        if (!recommendations.isEmpty()) {
            // Eng so'nggi tavsiyani olamiz (agar ro'yxat bo'lsa oxirgisi)
            aiMessage = recommendations.get(recommendations.size() - 1).getReason();
        }

        return ResTestResult.builder()
                .id(result.getId())
                .userId(result.getUsers().getId())
                .userName(result.getUsers().getFullName())
                
                // O'ZGARISH: .moduleName() emas, balki .categoryName() bo'ldi
                // Chunki endi Test Entity Module ga emas, Category ga bog'langan
                .categoryName(result.getTestSession().getCategory().getName())

                .scorePercent(result.getScorePercent())
                .aiRecommendation(aiMessage)
                .build();
    }
}