//package sfera.eduportal2.Payload.response;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ResAiRecommendation {
//
//    private Long userId;
//    private String userName;
//    private String userLevel;
//    private Double averageScore;
//    private String aiResponse;
//    private boolean success;
//    private String errorMessage;
//
//    /**
//     * Xatolik holati uchun qulay static factory method
//     */
//    public static ResAiRecommendation error(String message) {
//        return ResAiRecommendation.builder()
//                .success(false)
//                .errorMessage(message)
//                .build();
//    }
//}