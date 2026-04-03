package sfera.eduportal2.Payload.response;

import jakarta.persistence.ManyToOne;
import lombok.*;
import sfera.eduportal2.entity.Questions;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResOptions {
    @ManyToOne
    private Questions questions;

    private boolean isCorrect;

    private String text;
}
