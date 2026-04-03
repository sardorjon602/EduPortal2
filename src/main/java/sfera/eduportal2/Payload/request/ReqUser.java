package sfera.eduportal2.Payload.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ReqUser {

    @Schema(hidden = true)
    private Long id;

    @NotBlank(message = "Don't leave your first and last name blank.")
    private  String fullName;

    @Email(message = "Invalid email address entered.")
    private  String email;

    @Pattern(regexp = "^998(9[012345789]|6[125679]|7[01234569])[0-9]{7}$", message = "Phone number error")
    private String phoneNumber;

    private  String password;
}
