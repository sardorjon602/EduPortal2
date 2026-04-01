package sfera.eduportal2.Payload.response;

import lombok.*;

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

    private String imageUrl;

    private String role;


}
