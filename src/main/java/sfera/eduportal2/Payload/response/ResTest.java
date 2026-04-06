package sfera.eduportal2.Payload.response;

import lombok.*;
import sfera.eduportal2.entity.enums.Difficulty;

import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResTest {

    private Long id;

    private String moduleName;

    private Long userId;

    private Difficulty  difficulty;


    private Time timeLimit;

}
