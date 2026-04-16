package sfera.eduportal2.Payload.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResTestResult {

    private Long id;
    private Long userId;
    private String userName;
    private String categoryName;

    private int correctCount;
    private int totalCount;
    private double scorePercent;

    private String recommendedModule;
    private String aiRecommendation;

    private LocalDateTime takenAt;   // ← muhim
}