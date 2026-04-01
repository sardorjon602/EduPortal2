package sfera.eduportal2.Payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {

    private String token;

    private String role;

}
