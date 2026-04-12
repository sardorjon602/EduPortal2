package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class UserAnswer extends AbsEntity {

    @ManyToOne
    private TestSession testSession;

    @ManyToOne
    private Questions questions;

    @ManyToOne
    private Options selectedOption;

    private boolean isCorrect;
}