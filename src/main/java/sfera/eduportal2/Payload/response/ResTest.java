package sfera.eduportal2.Payload.response;

import lombok.*;
import sfera.eduportal2.entity.enums.Difficulty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResTest {

    private Long id;

    private String title;

    private String moduleName;

    private Difficulty  difficulty;

    private String optionName;

    private Double timeLimit;

}
