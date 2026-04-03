package sfera.eduportal2.Payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import sfera.eduportal2.entity.enums.Level;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AuthRegister {

    @NotBlank(message = "Don't leave your first and last name blank.")
    private  String fullName;

    @NotBlank(message = "Invalid Age entered.")
    private Integer age;

    @Email(message = "Invalid email address entered.")
    private  String email;

    @Pattern(regexp = "^998(9[012345789]|6[125679]|7[01234569])[0-9]{7}$", message = "Phone number error")
    private String phoneNumber;

    @NotBlank(message = "Leve invalid. ")
    private Level level;

    private  String password;
}
