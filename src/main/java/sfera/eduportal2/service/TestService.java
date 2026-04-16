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

import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.enums.Type;


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

    private final OptionsRepository optionsRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

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

        long timeLimitSeconds = (long) questionCount * 60;

        TestSession testSession = testSessionRepository.save(
                TestSession.builder()
                        .users(user)
                        .category(category)
                        .startTime(LocalDateTime.now())
                        .isFinished(false)
                        .build()
        );

        List<Long> questionIds = questions.stream()
                .map(Questions::getId)
                .collect(Collectors.toList());

        List<Options> allOptions = optionsRepository
                .findAllByQuestionsIdIn(questionIds);

        Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestions().getId()));

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
                .body(ResStartTest.builder()
                        .sessionId(testSession.getId())
                        .categoryName(category.getName())
                        .timeLimitSeconds(timeLimitSeconds)
                        .questions(questionDtos)
                        .build())
                .build();
    }

    // ====================================================================
    // TESTNI YAKUNLASH VA AI TAVSIYASI
    // ====================================================================
    public ApiResponse stopTest(ReqStopTest req) {

        if (req.getSessionId() == null || req.getUserId() == null) {
            return ApiResponse.builder()
                    .message("SessionId va UserId bo'sh bo'lmasligi kerak!")

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


        TestSession testSession = sessionOpt.get();

        // answers va textAnswers null bo'lsa bo'sh Map
        Map<Long, Long> answers = req.getAnswers() != null
                ? req.getAnswers() : new HashMap<>();
        Map<Long, String> textAnswers = req.getTextAnswers() != null
                ? req.getTextAnswers() : new HashMap<>();

        // Barcha questionId lar
        Set<Long> allQuestionIds = new HashSet<>();
        allQuestionIds.addAll(answers.keySet());
        allQuestionIds.addAll(textAnswers.keySet());

        if (allQuestionIds.isEmpty()) {
            return ApiResponse.builder()
                    .message("Hech qanday javob berilmadi!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        List<Questions> questionsList = questionsRepository
                .findAllById(new ArrayList<>(allQuestionIds));
        List<Options> allOptions = optionsRepository
                .findAllByQuestionsIdIn(new ArrayList<>(answers.keySet()));

        Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestions().getId()));

        int correct = 0;
        int total = questionsList.size();
        Map<Long, ModuleStats> moduleStatsMap = new LinkedHashMap<>();
        StringBuilder optionPrompt = new StringBuilder();
        StringBuilder textPrompt = new StringBuilder();
        List<UserAnswer> userAnswers = new ArrayList<>();

        for (Questions q : questionsList) {
            String moduleName = q.getModule() != null
                    ? q.getModule().getModuleName() : "Noma'lum";

            if (q.getType() == Type.OPTION) {
                // ---- OPTION TYPE — DB da tekshiramiz ----
                Long selectedOptId = answers.get(q.getId());
                List<Options> opts = optionsByQuestionId
                        .getOrDefault(q.getId(), List.of());

                Options selectedOption = opts.stream()
                        .filter(opt -> opt.getId().equals(selectedOptId))
                        .findFirst().orElse(null);

                String selectedText = selectedOption != null
                        ? selectedOption.getText() : "Javob berilmagan";
                String correctText = opts.stream()
                        .filter(Options::isCorrect)
                        .map(Options::getText)
                        .findFirst().orElse("Noma'lum");

                boolean isCorrect = selectedOption != null && selectedOption.isCorrect();
                if (isCorrect) correct++;

                userAnswers.add(UserAnswer.builder()
                        .testSession(testSession)
                        .questions(q)
                        .selectedOption(selectedOption)
                        .isCorrect(isCorrect)
                        .build());

                if (q.getModule() != null) {
                    moduleStatsMap.computeIfAbsent(
                            q.getModule().getId(),
                            id -> new ModuleStats(q.getModule()));
                    moduleStatsMap.get(q.getModule().getId()).addResult(isCorrect);
                }

                optionPrompt.append("Modul: ").append(moduleName).append("\n");
                optionPrompt.append("Savol: ").append(q.getText()).append("\n");
                optionPrompt.append("Berilgan javob: ").append(selectedText).append("\n");
                optionPrompt.append("To'g'ri javob: ").append(correctText).append("\n");
                optionPrompt.append("Natija: ")
                        .append(isCorrect ? "TO'G'RI" : "NOTO'G'RI").append("\n\n");

            } else if (q.getType() == Type.TEXT) {
                // ---- TEXT TYPE — AI tekshiradi ----
                String userText = textAnswers.getOrDefault(
                        q.getId(), "Javob berilmagan");

                userAnswers.add(UserAnswer.builder()
                        .testSession(testSession)
                        .questions(q)
                        .textAnswer(userText)
                        .isCorrect(false)
                        .build());

                if (q.getModule() != null) {
                    moduleStatsMap.computeIfAbsent(
                            q.getModule().getId(),
                            id -> new ModuleStats(q.getModule()));
                }

                textPrompt.append("Modul: ").append(moduleName).append("\n");
                textPrompt.append("Savol: ").append(q.getText()).append("\n");
                textPrompt.append("User javobi: ").append(userText).append("\n\n");
            }
        }

        userAnswerRepository.saveAll(userAnswers);
        testSession.setFinished(true);
        testSession.setEndTime(LocalDateTime.now());
        testSession.setUserAnswers(userAnswers);
        testSessionRepository.save(testSession);

        double scorePercent = total > 0 ? (correct * 100.0 / total) : 0.0;

        // AI ga yuborish uchun to'liq prompt
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(String.format(
                "Umumiy natija: %d/%d (%.1f%%)\n\n", correct, total, scorePercent));

        if (optionPrompt.length() > 0) {
            fullPrompt.append("OPTION SAVOLLAR:\n").append(optionPrompt);
        }
        if (textPrompt.length() > 0) {
            fullPrompt.append("TEXT SAVOLLAR (siz tekshiring):\n").append(textPrompt);
        }

        String aiRecommendation = callGeminiApi(fullPrompt.toString());

        Module weakestModule = moduleStatsMap.values().stream()
                .max(Comparator.comparingDouble(ModuleStats::errorRate))
                .map(ModuleStats::getModule).orElse(null);

        testResultRepository.save(TestResult.builder()
                .users(testSession.getUsers())
                .testSession(testSession)
                .correctCount(correct)
                .totalCount(total)
                .scorePercent(scorePercent)
                .aiRecommendation(aiRecommendation)
                .recommendedModule(weakestModule != null
                        ? weakestModule.getModuleName() : "Aniqlanmadi")
                .build());

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

                .body(ResStopTest.builder()
                        .correctCount(correct)
                        .totalCount(total)
                        .scorePercent(scorePercent)
                        .recommendedModule(weakestModule != null
                                ? weakestModule.getModuleName() : "Aniqlanmadi")
                        .aiRecommendation(aiRecommendation)
                        .build())
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
                    Siz ta'lim bo'yicha AI maslahatchisiz.
                    
                    OPTION savollar allaqachon tekshirilgan.
                    TEXT savollar uchun user javobini tekshirib to'g'ri/noto'g'ri aniqlang.
                    
                    Quyidagi formatda javob bering:
                    1. UMUMIY BAHO: (2-3 jumlada)
                    2. ZAIF TOMONLAR: (qaysi modullarda xato ko'p)
                    3. TAVSIYA ETILGAN MODUL: (modul nomi)
                    4. SABAB: (nima uchun)
                    5. KEYINGI QADAM: (maslahat)
                    
                    Javob o'zbek tilida, rag'batlantiruvchi bo'lsin.
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

                .body(responseData)
                .build();
    }

}