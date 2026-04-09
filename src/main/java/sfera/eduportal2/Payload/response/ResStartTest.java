package sfera.eduportal2.Payload.response;
import lombok.Builder;
import lombok.Data;
import java.sql.Time;
import java.util.List;

@Data
@Builder
public class ResStartTest {
    private Long sessionId; // Testni yakunlash (stop) uchun kerak
    private String moduleName;
    private Time timeLimit;
    private List<ResQuestions> questions;
}