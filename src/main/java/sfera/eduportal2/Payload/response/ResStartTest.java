package sfera.eduportal2.Payload.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResStartTest {
    private Long sessionId;
    private String categoryName;
    private Long timeLimitSeconds;
    private List<ResQuestions> questions;
}