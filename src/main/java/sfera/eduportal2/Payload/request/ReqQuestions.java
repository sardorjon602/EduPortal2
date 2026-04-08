package sfera.eduportal2.Payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sfera.eduportal2.entity.enums.Type;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqQuestions {
    private String text;
    private Type type;
    private Long moduleId;
}