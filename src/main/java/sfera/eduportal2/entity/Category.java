package sfera.eduportal2.entity;

import jakarta.persistence.*;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

@Entity
@Table()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends AbsEntity {


    private String name;


    private Integer questionCount;


}
