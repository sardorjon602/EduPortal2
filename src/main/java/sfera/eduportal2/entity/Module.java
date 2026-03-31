package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import lombok.*;
import sfera.eduportal2.entity.template.AbsEntity;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Module extends AbsEntity {
}
