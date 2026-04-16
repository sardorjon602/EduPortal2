package sfera.eduportal2.Payload.response;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResStartTest {
    private Long sessionId; // Testni yakunlash (stop) uchun kerak
    private String categoryName;
    private Long timeLimitSeconds;
    private List<ResQuestions> questions;
}