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
public class TestResult extends AbsEntity {
    @ManyToOne
    private Users user;

    @ManyToOne
    private Test test;

    @ManyToOne
    private Options selectedOption;
}
