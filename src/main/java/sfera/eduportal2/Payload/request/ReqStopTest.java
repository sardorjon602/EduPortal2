package sfera.eduportal2.Payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqStopTest {
    private Long sessionId;
    private Long userId;

    @Schema(example = "{\"1\": 2, \"3\": 4}")
    private Map<Long, Long> answers; // OPTION type: { questionId: optionId }

    @Schema(example = "{\"2\": \"Java bu dasturlash tili\"}")
    private Map<Long, String> textAnswers; // TEXT type: { questionId: "javob" }
}