package sfera.eduportal2.Payload.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResStopTest {
    private int correctAnswers;
    private int totalQuestions;
    private String scorePercent;
    private String recommendedModule;
    private String aiRecommendation;
}