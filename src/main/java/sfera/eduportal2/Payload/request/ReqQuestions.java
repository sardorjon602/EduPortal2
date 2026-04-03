package sfera.eduportal2.Payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqQuestions {
    private String text;
    private String type;
    private int questionCount;
    private Long moduleId;
}