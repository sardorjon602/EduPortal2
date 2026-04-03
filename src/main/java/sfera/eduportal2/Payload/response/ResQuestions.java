package sfera.eduportal2.Payload.response;

import jakarta.persistence.ManyToOne;
import lombok.*;
import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.enums.Type;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResQuestions {

    private String text;

    private Type type;

    @ManyToOne
    private Module module;

    private Integer questionCount;

}
