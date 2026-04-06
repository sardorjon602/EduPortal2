package sfera.eduportal2.Payload.request;

import lombok.*;
import sfera.eduportal2.entity.enums.Difficulty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqTest {

    private String title;

    private Long moduleId;

    private Difficulty difficulty;

    private Long optionId;

    private Double timeLimit;

}
