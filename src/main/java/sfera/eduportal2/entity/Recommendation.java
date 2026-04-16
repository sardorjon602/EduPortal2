package sfera.eduportal2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    private Users users;

    @ManyToOne
    private Module module;

    private String reason;


}
