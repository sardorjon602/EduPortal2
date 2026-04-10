package sfera.eduportal2.Payload.response;

import lombok.*;
import sfera.eduportal2.entity.enums.Type;
import java.util.List;

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
    private List<ResOptions> options;
}