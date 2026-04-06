package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Recommendation extends AbsEntity {

    @OneToOne
    private Users users;

    @OneToOne
    private Module module;

    private String reason;


}
