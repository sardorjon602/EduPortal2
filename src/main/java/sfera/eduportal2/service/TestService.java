package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.Payload.request.ReqTest;
import sfera.eduportal2.Payload.response.ResTest;
import sfera.eduportal2.Repository.*;
import sfera.eduportal2.entity.*;
import sfera.eduportal2.entity.Module;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository usersRepository;
    private final QuestionsRepository questionsRepository;
    private final TestResultRepository testResultRepository;
    private final RecommendationService recommendationService;

    // ====================================================================
    // 1. ADMIN QISMI (Testlarni boshqarish - CRUD)
    // ====================================================================

    // Admin test yaratadi (Module ID va TimeLimit bilan)
    public ApiResponse createTest(ReqTest reqTest) {
        Optional<Module> moduleOptional = moduleRepository.findById(reqTest.getModuleId());
        if (moduleOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        // Testni yaratgan adminni saqlash (ixtiyoriy)
        Optional<Users> adminOptional = usersRepository.findById(reqTest.getUserId());
        if (adminOptional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Admin (User) topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Test test = Test.builder()
                .user(adminOptional.get()) // Admin ID
                .module(moduleOptional.get())
                .timeLimit(reqTest.getTimeLimit()) // Masalan: 00:30:00 (30 daqiqa)
                .build();

        testRepository.save(test);

        return ApiResponse.builder()
                .message("Test muvaffaqiyatli yaratildi")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();
    }

    public ApiResponse updateTest(Long id, ReqTest reqTest) {
        Optional<Test> testOpt = testRepository.findById(id);
        if (testOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Optional<Module> moduleOpt = moduleRepository.findById(reqTest.getModuleId());
        if (moduleOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Test test = testOpt.get();
        test.setModule(moduleOpt.get());
        test.setTimeLimit(reqTest.getTimeLimit());
        testRepository.save(test);

        return ApiResponse.builder()
                .message("Test muvaffaqiyatli yangilandi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse deleteTest(Long id) {
        if (!testRepository.existsById(id)) {
            return ApiResponse.builder()
                    .message("Test topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        testRepository.deleteById(id);
        return ApiResponse.builder()
                .message("Test o'chirildi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse getAllTests() {
        List<Test> tests = testRepository.findAll();
        List<ResTest> resTests = new ArrayList<>();
        for (Test test : tests) {
            resTests.add(ResTest.builder()
                    .id(test.getId())
                    .userId(test.getUser().getId())
                    .moduleName(test.getModule().getModuleName())
                    .timeLimit(test.getTimeLimit())
                    .build());
        }
        return ApiResponse.builder().message("Success").success(true).status(HttpStatus.OK).body(resTests).build();
    }

    // ====================================================================
    // 2. O'QUVCHI (USER) QISMI (Test ishlash, AI tekshiruvi)
    // ====================================================================

    // O'quvchi testni boshlaydi
    public ApiResponse startTest(ReqStartTest req) {
        Optional<Test> testOpt = testRepository.findById(req.getTestId()); // Module bo'yicha testni qidiramiz
        Optional<Users> userOpt = usersRepository.findById(req.getUserId());

        if (testOpt.isEmpty() || userOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test yoki O'quvchi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Test test = testOpt.get();
        
        // Modulga tegishli barcha savollarni olish
        List<Questions> questions = questionsRepository.findAllByModuleId(test.getModule().getId());
        if (questions.isEmpty()) {
            return ApiResponse.builder()
                    .message("Bu test uchun hali savollar kiritilmagan")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        // Test sessiyasini (TestResult) ochamiz, ball hozircha 0
        TestResult activeSession = TestResult.builder()
                .users(userOpt.get())
                .test(test)
                .score(0.0)
                .takenAt(LocalDateTime.now()) // Boshlanish vaqti
                .build();
        testResultRepository.save(activeSession);

        // Front-end uchun kerakli ma'lumotlarni yuboramiz
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sessionId", activeSession.getId()); // Testni yakunlash uchun kerak
        responseData.put("timeLimit", test.getTimeLimit());
        responseData.put("questions", questions);

        return ApiResponse.builder()
                .message("Test boshlandi")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseData)
                .build();
    }

    // O'quvchi testni yakunlaydi va AI tavsiya beradi
    public ApiResponse stopTest(ReqStopTest req) {
        Optional<TestResult> sessionOpt = testResultRepository.findById(req.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Faol test sessiyasi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestResult session = sessionOpt.get();
        Test test = session.getTest();

        // 1. Natijani (ballni) saqlaymiz
        session.setScore(req.getScore());
        session.setTakenAt(LocalDateTime.now()); // Tugatilgan vaqtni yangilaymiz
        testResultRepository.save(session);

        // 2. AI Recommendation xizmatini chaqirib tavsiya olamiz
        // (Sizdagi RecommendationService ishlab turgan bo'lishi kerak)
        var aiRecommendation = recommendationService.generateAndSave(session.getUsers().getId());

        // 3. O'quvchiga yakuniy natija va AI maslahatini qaytaramiz
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("finalScore", session.getScore());
        responseData.put("aiRecommendation", aiRecommendation);

        return ApiResponse.builder()
                .message("Test yakunlandi. AI sizga tavsiya tayyorladi!")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseData)
                .build();
    }
}