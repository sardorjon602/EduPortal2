package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class TestResult extends AbsEntity {

    @ManyToOne
    private Users users;

    @OneToOne
    private Test test;

    private Double score;

    private LocalDateTime takenAt;
}
