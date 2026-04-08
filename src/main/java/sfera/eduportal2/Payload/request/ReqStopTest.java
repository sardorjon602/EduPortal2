package sfera.eduportal2.Payload.request;

import lombok.Data;
import java.util.Map;

@Data
public class ReqStopTest {
    private Long testId;
    private Long sessionId;
    private Long userId;
    private Double score;
}