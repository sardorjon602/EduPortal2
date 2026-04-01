package sfera.eduportal2.Payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthLogin {

    @Email
    private String email;

    @NotBlank
    private String password;
}
