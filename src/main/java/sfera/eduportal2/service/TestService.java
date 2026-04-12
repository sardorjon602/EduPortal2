package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.Payload.response.ResQuestions;
import sfera.eduportal2.Payload.response.ResStartTest;
import sfera.eduportal2.Repository.*;
import sfera.eduportal2.entity.*;
// import sfera.eduportal2.entity.Module; // Bunga endi ehtiyoj yo'q, o'rniga Category ishlatamiz

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final CategoryRepository categoryRepository; // Modul o'rniga Kategoriya repozitoriysi
    private final UserRepository usersRepository;
    private final QuestionsRepository questionsRepository;
    private final TestResultRepository testResultRepository;
    private final RecommendationService recommendationService;

    // ====================================================================
    // TESTNI BOSHLASH (KATEGORIYA BO'YICHA)
    // ====================================================================
    public ApiResponse startTest(ReqStartTest req) {
        // 6-band: userId va categoryId xatosini to'liq nazorat qilish
        if (req.getUserId() == null || req.getCategoryId() == null) {
            return ApiResponse.builder()
                    .message("UserId yoki CategoryId bo'sh bo'lishi mumkin emas!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<Users> userOpt = usersRepository.findById(req.getUserId());
        Optional<Category> categoryOpt = categoryRepository.findById(req.getCategoryId());

        if (userOpt.isEmpty() || categoryOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Foydalanuvchi yoki Kategoriya tizimda topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Users user = userOpt.get();
        Category category = categoryOpt.get();

        // Kategoriya ichidagi BARCHA modullarning savollarini olish
        List<Questions> questions = questionsRepository.findAllByModule_CategoryId(category.getId());
        if (questions.isEmpty()) {
            return ApiResponse.builder()
                    .message(category.getName() + " kategoriyasi uchun hali savollar kiritilmagan")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        // Vaqtni hisoblash (har bir savolga 1 daqiqadan beramiz)
        int questionCount = questions.size();
        long durationMillis = questionCount * 60 * 1000L;
        Time timeLimit = new Time(System.currentTimeMillis() + durationMillis);

        // 8-band: Test sessiyasini avtomatik orqa fonda yaratamiz (Endi Kategoriyaga ulanadi)
        Test testSession = Test.builder()
                .user(user)
                .category(category)
                .timeLimit(timeLimit)
                .build();
        testRepository.save(testSession);

        // Natija (score) ni saqlash uchun qolip ochamiz
        TestResult activeSession = TestResult.builder()
                .users(user)
                .test(testSession)
                .score(0.0)
                .takenAt(LocalDateTime.now())
                .build();
        testResultRepository.save(activeSession);

        // 10-band: Entity o'rniga DTO (ResQuestions) yasaymiz
        List<ResQuestions> questionDtos = questions.stream().map(q ->
                ResQuestions.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .type(q.getType())
                        .build()
        ).collect(Collectors.toList());

        ResStartTest responseDto = ResStartTest.builder()
                .sessionId(activeSession.getId())
                .categoryName(category.getName()) // moduleName emas, categoryName
                .timeLimit(timeLimit)
                .questions(questionDtos)
                .build();

        // 7-band: Aniq va tushunarli ma'lumot (xabar) qaytarish
        String exactMessage = String.format("'%s' kategoriyasi bo'yicha test muvaffaqiyatli boshlandi. Sizda %d ta savol va %d daqiqa vaqt bor.",
                category.getName(), questionCount, questionCount);

        return ApiResponse.builder()
                .message(exactMessage)
                .success(true)
                .status(HttpStatus.OK)
                .body(responseDto)
                .build();
    }

    // ====================================================================
    // TESTNI YAKUNLASH VA AI TAVSIYASI
    // ====================================================================
    public ApiResponse stopTest(ReqStopTest req) {
        if (req.getSessionId() == null || req.getScore() == null) {
            return ApiResponse.builder()
                    .message("SessionId va Score kiritilishi shart!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<TestResult> sessionOpt = testResultRepository.findById(req.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Faol test sessiyasi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestResult session = sessionOpt.get();

        // Natijani saqlaymiz
        session.setScore(req.getScore());
        session.setTakenAt(LocalDateTime.now());
        testResultRepository.save(session);

        // AI Recommendation xizmatini chaqirib tavsiya olamiz
        var aiRecommendation = recommendationService.generateAndSave(session.getUsers().getId());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("finalScore", session.getScore());
        responseData.put("aiRecommendation", aiRecommendation);

        return ApiResponse.builder()
                .message("Test yakunlandi. AI natijangizni tahlil qildi.")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseData)
                .build();
    }
}