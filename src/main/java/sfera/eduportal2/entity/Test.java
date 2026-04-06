package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;
import sfera.eduportal2.entity.enums.Difficulty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Test extends AbsEntity {

    private String title;

    @ManyToOne
    private Module module;

    private Difficulty difficulty;

    @ManyToOne
    private Options options;

    private Double timeLimit;
}