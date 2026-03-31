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
public class Lesson extends AbsEntity {

    private String title;

    private String description;

    @ManyToOne
    private Category category;

    private String difficultyLevel;



}
