package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Test extends AbsEntity {

    @ManyToOne
    private Category category;

    private String title;

    @ManyToOne
    private Module module;

    private Date timeLimit;

    private Long testCount;

    private boolean active;




}

