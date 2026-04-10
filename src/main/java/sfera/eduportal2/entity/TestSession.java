package sfera.eduportal2.entity;

import jakarta.persistence.*;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class TestSession extends AbsEntity {

    @ManyToOne
    private Users users;

    @ManyToOne
    private Category category;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isFinished;

    @OneToMany
    private List<UserAnswer> userAnswers;
}