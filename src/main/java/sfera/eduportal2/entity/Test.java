package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;
import sfera.eduportal2.entity.enums.Difficulty;

import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Test extends AbsEntity {

    String name;

    @ManyToOne
    private Users user;

    @ManyToOne
    private Module module;

    private Time timeLimit;

}