package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqStartTest;
import sfera.eduportal2.Payload.request.ReqStopTest;
import sfera.eduportal2.Payload.response.*;
import sfera.eduportal2.Repository.*;
import sfera.eduportal2.entity.*;
import sfera.eduportal2.entity.Module;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestSessionRepository testSessionRepository;
    private final TestResultRepository testResultRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository usersRepository;
    private final QuestionsRepository questionsRepository;
    private final OptionsRepository optionsRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // ================================================================
    // TESTNI BOSHLASH
    // ================================================================
    public ApiResponse startTest(ReqStartTest req) {
        if (req.getUserId() == null || req.getCategoryId() == null) {
            return ApiResponse.builder()
                    .message("UserId yoki CategoryId bo'sh bo'lmasligi kerak!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<Users> userOpt = usersRepository.findById(req.getUserId());
        Optional<Category> categoryOpt = categoryRepository.findById(req.getCategoryId());

        if (userOpt.isEmpty() || categoryOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Foydalanuvchi yoki Kategoriya topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Users user = userOpt.get();
        Category category = categoryOpt.get();

        List<Questions> questions = questionsRepository
                .findByModuleCategoryId(category.getId());

        if (questions.isEmpty()) {
            return ApiResponse.builder()
                    .message(category.getName() + " kategoriyasi uchun savollar topilmadi")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        int questionCount = questions.size();
        long timeLimitSeconds = (long) questionCount * 60;

        // TestSession saqlaymiz
        TestSession testSession = testSessionRepository.save(
                TestSession.builder()
                        .users(user)
                        .category(category)
                        .startTime(LocalDateTime.now())
                        .isFinished(false)
                        .build()
        );

        // Optionlarni BATCH da olamiz
        List<Long> questionIds = questions.stream()
                .map(Questions::getId)
                .collect(Collectors.toList());

        List<Options> allOptions = optionsRepository
                .findAllByQuestionsIdIn(questionIds);

        Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestions().getId()));

        // DTO — isCorrect false qilib yuboramiz
        List<ResQuestions> questionDtos = questions.stream().map(q -> {
            List<ResOptions> optionDtos = optionsByQuestionId
                    .getOrDefault(q.getId(), List.of())
                    .stream()
                    .map(opt -> ResOptions.builder()
                            .id(opt.getId())
                            .text(opt.getText())
                            .isCorrect(false) // YASHIRILDI
                            .questionId(q.getId())
                            .questionText(q.getText())
                            .build())
                    .collect(Collectors.toList());

            return ResQuestions.builder()
                    .id(q.getId())
                    .text(q.getText())
                    .type(q.getType())
                    .moduleId(q.getModule().getId())
                    .moduleName(q.getModule().getModuleName())
                    .options(optionDtos)
                    .build();
        }).collect(Collectors.toList());

        ResStartTest responseDto = ResStartTest.builder()
                .sessionId(testSession.getId())
                .categoryName(category.getName())
                .timeLimitSeconds(timeLimitSeconds)
                .questions(questionDtos)
                .build();

        return ApiResponse.builder()
                .message(String.format(
                        "'%s' kategoriyasi bo'yicha test boshlandi. %d ta savol, %d daqiqa.",
                        category.getName(), questionCount, questionCount))
                .success(true)
                .status(HttpStatus.OK)
                .body(responseDto)
                .build();
    }

    // ================================================================
    // TESTNI YAKUNLASH + AI TAVSIYA
    // ================================================================
    public ApiResponse stopTest(ReqStopTest req) {
        if (req.getSessionId() == null || req.getUserId() == null
                || req.getAnswers() == null || req.getAnswers().isEmpty()) {
            return ApiResponse.builder()
                    .message("SessionId, UserId va Answers bo'sh bo'lmasligi kerak!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<TestSession> sessionOpt = testSessionRepository
                .findById(req.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test sessiyasi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestSession testSession = sessionOpt.get();
        Map<Long, Long> answers = req.getAnswers();

        // Savollar va optionlarni BATCH da olamiz
        List<Long> questionIds = new ArrayList<>(answers.keySet());
        List<Questions> questionsList = questionsRepository.findAllById(questionIds);
        List<Options> allOptions = optionsRepository.findAllByQuestionsIdIn(questionIds);

        Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestions().getId()));

        // ---- SCORE HISOBLASH ----
        int correct = 0;
        int total = questionsList.size();
        Map<Long, ModuleStats> moduleStatsMap = new LinkedHashMap<>();
        StringBuilder promptBody = new StringBuilder();
        List<UserAnswer> userAnswers = new ArrayList<>();

        for (Questions q : questionsList) {
            Long selectedOptId = answers.get(q.getId());
            List<Options> opts = optionsByQuestionId
                    .getOrDefault(q.getId(), List.of());

            // Tanlangan option
            Options selectedOption = opts.stream()
                    .filter(opt -> opt.getId().equals(selectedOptId))
                    .findFirst()
                    .orElse(null);

            String selectedText = selectedOption != null
                    ? selectedOption.getText() : "Javob berilmagan";

            String correctText = opts.stream()
                    .filter(Options::isCorrect)
                    .map(Options::getText)
                    .findFirst()
                    .orElse("Noma'lum");

            boolean isCorrect = selectedOption != null && selectedOption.isCorrect();
            if (isCorrect) correct++;

            // UserAnswer saqlaymiz
            userAnswers.add(UserAnswer.builder()
                    .testSession(testSession)
                    .questions(q)
                    .selectedOption(selectedOption)
                    .isCorrect(isCorrect)
                    .build());

            // Modul statistikasi
            if (q.getModule() != null) {
                moduleStatsMap.computeIfAbsent(
                        q.getModule().getId(),
                        id -> new ModuleStats(q.getModule()));
                moduleStatsMap.get(q.getModule().getId()).addResult(isCorrect);
            }

            String moduleName = q.getModule() != null
                    ? q.getModule().getModuleName() : "Noma'lum";

            promptBody.append("Modul: ").append(moduleName).append("\n");
            promptBody.append("Savol: ").append(q.getText()).append("\n");
            promptBody.append("Berilgan javob: ").append(selectedText).append("\n");
            promptBody.append("To'g'ri javob: ").append(correctText).append("\n");
            promptBody.append("Natija: ")
                    .append(isCorrect ? "TO'G'RI" : "NOTO'G'RI").append("\n\n");
        }

        // UserAnswer larni saqlaymiz
        userAnswerRepository.saveAll(userAnswers);

        // TestSession ni yakunlaymiz
        testSession.setFinished(true);
        testSession.setEndTime(LocalDateTime.now());
        testSession.setUserAnswers(userAnswers);
        testSessionRepository.save(testSession);

        double scorePercent = total > 0 ? (correct * 100.0 / total) : 0.0;

        String fullPrompt = String.format(
                "Umumiy natija: %d/%d (%.1f%%)\n\n", correct, total, scorePercent)
                + promptBody;

        // Eng zaif modul
        Module weakestModule = moduleStatsMap.values().stream()
                .max(Comparator.comparingDouble(ModuleStats::errorRate))
                .map(ModuleStats::getModule)
                .orElse(null);

        // Gemini ga yuboramiz
        String aiRecommendation = callGeminiApi(fullPrompt);

        // TestResult saqlaymiz
        testResultRepository.save(
                TestResult.builder()
                        .users(testSession.getUsers())
                        .testSession(testSession)
                        .correctCount(correct)
                        .totalCount(total)
                        .scorePercent(scorePercent)
                        .aiRecommendation(aiRecommendation)
                        .recommendedModule(weakestModule != null
                                ? weakestModule.getModuleName() : "Aniqlanmadi")
                        .build()
        );

        ResStopTest responseDto = ResStopTest.builder()
                .correctCount(correct)
                .totalCount(total)
                .scorePercent(scorePercent)
                .recommendedModule(weakestModule != null
                        ? weakestModule.getModuleName() : "Aniqlanmadi")
                .aiRecommendation(aiRecommendation)
                .build();

        return ApiResponse.builder()
                .message("Test yakunlandi! AI natijangizni tahlil qildi.")
                .success(true)
                .status(HttpStatus.OK)
                .body(responseDto)
                .build();
    }

    // ================================================================
    // GEMINI API
    // ================================================================
    private String callGeminiApi(String testSummary) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String systemPrompt = """
                    O'quvchining test natijalarini tahlil qilib, \
                    quyidagi formatda javob ber:
                    
                    1. UMUMIY BAHO: (2-3 jumlada umumiy natijani baholash)
                    2. ZAIF TOMONLAR: (qaysi modullarda ko'proq xato ketgan)
                    3. TAVSIYA ETILGAN MODUL: (o'qishi kerak bo'lgan modul nomi)
                    4. SABAB: (nima uchun aynan shu modul)
                    5. KEYINGI QADAM: (qanday o'qish kerak, maslahat)
                    
                    Javob o'zbek tilida, rag'batlantiruvchi va aniq bo'lsin.
                    """;

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", systemPrompt + "\n\n" + testSummary)
                            ))
                    )
            );

            String urlWithKey = GEMINI_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    urlWithKey, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content =
                            (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts =
                            (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            return "AI tavsiyasini olishda xatolik: " + e.getMessage();
        }
        return "AI tavsiyasi mavjud emas";
    }

    // ================================================================
    // HELPER
    // ================================================================
    private static class ModuleStats {
        private final Module module;
        private int total = 0;
        private int errors = 0;

        ModuleStats(Module module) { this.module = module; }

        void addResult(boolean isCorrect) {
            total++;
            if (!isCorrect) errors++;
        }

        double errorRate() { return total > 0 ? (double) errors / total : 0; }
        Module getModule() { return module; }
    }
}