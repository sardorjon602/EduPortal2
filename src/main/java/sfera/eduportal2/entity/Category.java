package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Category extends AbsEntity {
private String name;

}
