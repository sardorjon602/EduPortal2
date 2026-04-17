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
import sfera.eduportal2.entity.enums.Type;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestSessionRepository testSessionRepository;
    private final TestResultRepository testResultRepository;
    private final CategoryRepository categoryRepository;
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
    public ApiResponse startTest(ReqStartTest req, Users currentUser) {
        if (req.getCategoryId() == null) {
            return ApiResponse.builder()
                    .message("CategoryId bo'sh bo'lmasligi kerak!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<Category> categoryOpt = categoryRepository.findById(req.getCategoryId());
        if (categoryOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Kategoriya topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Category category = categoryOpt.get();

        List<Questions> questions = questionsRepository.findByModuleCategoryId(category.getId());

        if (questions.isEmpty()) {
            return ApiResponse.builder()
                    .message(category.getName() + " kategoriyasi uchun savollar topilmadi")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        int questionCount = questions.size();
        long timeLimitSeconds = (long) questionCount * 60;

        TestSession testSession = testSessionRepository.save(
                TestSession.builder()
                        .users(currentUser)
                        .category(category)
                        .startTime(LocalDateTime.now())
                        .isFinished(false)
                        .build()
        );

        List<Long> optionQuestionIds = questions.stream()
                .filter(q -> q.getType() == Type.OPTION)
                .map(Questions::getId)
                .collect(Collectors.toList());

        List<Options> allOptions = optionQuestionIds.isEmpty()
                ? List.of()
                : optionsRepository.findAllByQuestionsIdIn(optionQuestionIds);

        Map<Long, List<Options>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestions().getId()));

        List<ResQuestions> questionDtos = questions.stream().map(q -> {
            List<ResOptions> optionDtos = optionsByQuestionId
                    .getOrDefault(q.getId(), List.of())
                    .stream()
                    .map(opt -> ResOptions.builder()
                            .id(opt.getId())
                            .text(opt.getText())
                            .isCorrect(false)
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

        return ApiResponse.builder()
                .message(String.format(
                        "'%s' kategoriyasi bo'yicha test boshlandi. %d ta savol, %d daqiqa.",
                        category.getName(), questionCount, questionCount))
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

    // ================================================================
    // TESTNI YAKUNLASH + AI TAVSIYA
    // ================================================================
    public ApiResponse stopTest(ReqStopTest req, Users currentUser) {
        if (req.getSessionId() == null) {
            return ApiResponse.builder()
                    .message("SessionId bo'sh bo'lmasligi kerak!")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Optional<TestSession> sessionOpt = testSessionRepository.findById(req.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("Test sessiyasi topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        TestSession testSession = sessionOpt.get();

        if (!testSession.getUsers().getId().equals(currentUser.getId())) {
            return ApiResponse.builder()
                    .message("Bu sessiya sizga tegishli emas!")
                    .success(false)
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        Map<Long, Long> answers = req.getAnswers() != null
                ? req.getAnswers() : new HashMap<>();
        Map<Long, String> textAnswers = req.getTextAnswers() != null
                ? req.getTextAnswers() : new HashMap<>();

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

        List<Long> optionQuestionIds = questionsList.stream()
                .filter(q -> q.getType() == Type.OPTION)
                .map(Questions::getId)
                .collect(Collectors.toList());

        List<Options> allOptions = optionQuestionIds.isEmpty()
                ? List.of()
                : optionsRepository.findAllByQuestionsIdIn(optionQuestionIds);

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

            // ── OPTION savollar ──────────────────────────────────────
            if (q.getType() == Type.OPTION) {
                Long selectedOptId = answers.get(q.getId());
                List<Options> opts = optionsByQuestionId.getOrDefault(q.getId(), List.of());

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

            // ── TEXT savollar — Gemini orqali tekshiriladi ───────────
            } else if (q.getType() == Type.TEXT) {
                String userText = textAnswers.getOrDefault(q.getId(), "");

                // Gemini javobni tekshiradi → true yoki false
                boolean isCorrect = evaluateTextAnswerWithGemini(q.getText(), userText);
                if (isCorrect) correct++;

                userAnswers.add(UserAnswer.builder()
                        .testSession(testSession)
                        .questions(q)
                        .textAnswer(userText)
                        .isCorrect(isCorrect)
                        .build());

                if (q.getModule() != null) {
                    moduleStatsMap.computeIfAbsent(
                            q.getModule().getId(),
                            id -> new ModuleStats(q.getModule()));
                    moduleStatsMap.get(q.getModule().getId()).addResult(isCorrect);
                }

                textPrompt.append("Modul: ").append(moduleName).append("\n");
                textPrompt.append("Savol: ").append(q.getText()).append("\n");
                textPrompt.append("User javobi: ").append(
                        userText.isBlank() ? "Javob berilmagan" : userText).append("\n");
                textPrompt.append("Natija: ")
                        .append(isCorrect ? "TO'G'RI ✓" : "NOTO'G'RI ✗").append("\n\n");
            }
        }

        userAnswerRepository.saveAll(userAnswers);
        testSession.setFinished(true);
        testSession.setEndTime(LocalDateTime.now());
        testSession.setUserAnswers(userAnswers);
        testSessionRepository.save(testSession);

        double scorePercent = total > 0 ? (correct * 100.0 / total) : 0.0;

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(String.format(
                "Umumiy natija: %d/%d (%.1f%%)\n\n", correct, total, scorePercent));
        if (optionPrompt.length() > 0) {
            fullPrompt.append("OPTION SAVOLLAR:\n").append(optionPrompt);
        }
        if (textPrompt.length() > 0) {
            fullPrompt.append("TEXT SAVOLLAR:\n").append(textPrompt);
        }

        String aiRecommendation = callGeminiForRecommendation(fullPrompt.toString());

        Module weakestModule = moduleStatsMap.values().stream()
                .max(Comparator.comparingDouble(ModuleStats::errorRate))
                .map(ModuleStats::getModule).orElse(null);

        testResultRepository.save(TestResult.builder()
                .users(currentUser)
                .testSession(testSession)
                .correctCount(correct)
                .totalCount(total)
                .scorePercent(scorePercent)
                .aiRecommendation(aiRecommendation)
                .recommendedModule(weakestModule != null
                        ? weakestModule.getModuleName() : "Aniqlanmadi")
                .takenAt(LocalDateTime.now())
                .build());

        return ApiResponse.builder()
                .message("Test yakunlandi! AI natijangizni tahlil qildi.")
                .success(true)
                .status(HttpStatus.OK)
                .body(ResStopTest.builder()
                        .correctAnswers(correct)
                        .totalQuestions(total)
                        .scorePercent(String.valueOf(scorePercent))
                        .recommendedModule(weakestModule != null
                                ? weakestModule.getModuleName() : "Aniqlanmadi")
                        .aiRecommendation(aiRecommendation)
                        .build())
                .build();
    }

    // ================================================================
    // GEMINI — TEXT javobni tekshirish (faqat true / false qaytaradi)
    // ================================================================
    private boolean evaluateTextAnswerWithGemini(String questionText, String userAnswer) {
        try {
            if (userAnswer == null || userAnswer.isBlank()) {
                return false;
            }

            String prompt = String.format("""
                    Sen ta'lim tekshiruvchisisан. Quyidagi savol va javobni ko'rib chiq.
                    
                    Savol: %s
                    
                    Foydalanuvchi javobi: %s
                    
                    Agar javob savolga mazmuniy jihatdan to'g'ri bo'lsa — faqat "true" yoz.
                    Agar noto'g'ri, to'liq emas yoki savolga aloqasiz bo'lsa — faqat "false" yoz.
                    Boshqa hech narsa yozma. Faqat bitta so'z: true yoki false
                    """, questionText, userAnswer);

            String result = callGeminiRaw(prompt);
            return result.toLowerCase().contains("true");

        } catch (Exception e) {
            System.err.println("Gemini text evaluation xatolik: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // GEMINI — Yakuniy tavsiya
    // ================================================================
    private String callGeminiForRecommendation(String testSummary) {
        String systemPrompt = """
                Siz ta'lim bo'yicha AI maslahatchisiz.
                Barcha savollar (option va text) allaqachon tekshirilgan.
                
                Quyidagi formatda javob bering:
                1. UMUMIY BAHO: (2-3 jumlada natijani baholash)
                2. ZAIF TOMONLAR: (qaysi modullarda xato ko'p)
                3. TAVSIYA ETILGAN MODUL: (birinchi o'qishi kerak bo'lgan modul nomi)
                4. SABAB: (nima uchun aynan shu modul)
                5. KEYINGI QADAM: (qanday o'qish kerak, amaliy maslahat)
                
                Javob o'zbek tilida, rag'batlantiruvchi va aniq bo'lsin.
                """;

        try {
            return callGeminiRaw(systemPrompt + "\n\n" + testSummary);
        } catch (Exception e) {
            return "AI tavsiyasini olishda xatolik: " + e.getMessage();
        }
    }

    // ================================================================
    // GEMINI — Asosiy chaqiruv (ikkala metod ham shu orqali ishlaydi)
    // ================================================================
    private String callGeminiRaw(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String urlWithKey = GEMINI_URL + "?key=" + apiKey;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content =
                        (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts =
                        (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text").toString().trim();
                }
            }
        }
        return "";
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