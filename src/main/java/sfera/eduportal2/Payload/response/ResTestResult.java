package sfera.eduportal2.Payload.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResTestResult {
    private Long id;
    private Long userId;
    private String userName;
    private String categoryName;
    private Double score;
    private LocalDateTime takenAt;
    // O'sha natijaga biriktirilgan AI tavsiyasini (agar bo'lsa) saqlaymiz
    private String aiRecommendationMessage; 
}