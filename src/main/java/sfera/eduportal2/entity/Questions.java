package sfera.eduportal2.entity;

import jakarta.persistence.*;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;
import sfera.eduportal2.entity.enums.Type;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Questions extends AbsEntity {

    private String text;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne
    private Module module;



}
