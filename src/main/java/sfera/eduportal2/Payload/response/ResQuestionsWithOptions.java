package sfera.eduportal2.Payload.response;

import lombok.*;
import sfera.eduportal2.entity.enums.Type;
import java.util.List;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class ResQuestionsWithOptions {
    private Long id;
    private String text;
    private Type type;
    private String moduleName;
    private List<ResOptionForTest> options;
}
