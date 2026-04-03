package sfera.eduportal2.Payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResQuestions {
    private String text;
    private String type;
    private int questionCount;
    private Long moduleId;
    private String moduleName;
}