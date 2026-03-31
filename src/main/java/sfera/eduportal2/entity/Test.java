package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Test extends AbsEntity {
    @ManyToOne
    private Module module;
    @OneToMany(mappedBy = "test")
    private List<Options> options;

    private String text;


}

