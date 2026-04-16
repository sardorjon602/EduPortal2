package sfera.eduportal2.entity;

import jakarta.persistence.*;
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
    private Options selectedOption; // OPTION type uchun

    @Column(columnDefinition = "TEXT")
    private String textAnswer; // TEXT type uchun

    private boolean isCorrect;
}