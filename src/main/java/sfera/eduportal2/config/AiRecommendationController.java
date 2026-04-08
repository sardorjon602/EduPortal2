//package sfera.eduportal2.controller;
//
//import org.springframework.web.bind.annotation.*;
//import sfera.eduportal2.service.EduPortalAiService;
//
//@RestController
//@RequestMapping("/api/v1/ai")
//public class AiRecommendationController {
//
//    private final EduPortalAiService aiService;
//
//    public AiRecommendationController(EduPortalAiService aiService) {
//        this.aiService = aiService;
//    }
//
//    // Web-saytdan keladigan so'rovlarni qabul qilish uchun DTO
//    public record RecommendationRequest(String studentName, int testScore, String interests) {}
//
//
//    @PostMapping("/recommend-course")
//    public String recommendCourse(@RequestBody RecommendationRequest request) {
//        // Front-end'dan kelgan ma'lumotlarni AI service'ga uzatamiz
//        return aiService.getCourseRecommendation(
//                request.studentName(),
//                request.testScore(),
//                request.interests()
//        );
//    }
//}