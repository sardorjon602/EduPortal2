package sfera.eduportal2.Payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sfera.eduportal2.entity.enums.Type;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResQuestions {
    private Long id;
    private String text;
    private Type type;
    private Long moduleId;
    private String moduleName;
}