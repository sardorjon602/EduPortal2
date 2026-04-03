package sfera.eduportal2.Payload.response;

import lombok.*;
import sfera.eduportal2.entity.enums.Level;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ResUser {

    private Long id;

    private String fullName;

    private String phoneNumber;

    private String email;

    private Integer age;

    private Level level;

    private String role;


}
