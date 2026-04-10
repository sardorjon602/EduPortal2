package sfera.eduportal2.Payload.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResStopTest {
    private int correctCount;
    private int totalCount;
    private double scorePercent;
    private String recommendedModule;
    private String aiRecommendation;
}