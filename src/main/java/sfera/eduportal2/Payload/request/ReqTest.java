package sfera.eduportal2.Payload.request;

import lombok.*;
import sfera.eduportal2.entity.enums.Difficulty;

import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqTest {


    private Long moduleId;

    private Long userId;

    private Difficulty difficulty;


    private Time timeLimit;

}
