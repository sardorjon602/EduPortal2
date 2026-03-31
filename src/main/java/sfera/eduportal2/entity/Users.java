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
public class Users extends AbsEntity {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private Integer age;

    private String role;

    private String level;

    private boolean is_active;
}
