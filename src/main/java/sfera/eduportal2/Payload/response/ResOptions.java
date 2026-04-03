package sfera.eduportal2.Payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResOptions {
    private String text;
    private boolean isCorrect;
    private Long questionId;
    private String questionText;
}