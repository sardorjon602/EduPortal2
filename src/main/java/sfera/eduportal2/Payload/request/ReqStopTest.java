package sfera.eduportal2.Payload.request;

import lombok.*;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqStopTest {
    private Long sessionId;
    private Long userId;
    private Map<Long, Long> answers; // { questionId: optionId }
}