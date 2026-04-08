package sfera.eduportal2.Security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "EduPortal", version = "v1",
                description = "This swagger is fo EduPortal",
                contact = @Contact(name = "Haqqulov Sardor", url = "https://t.me/sardor_coder00", email = "haqqulovsardor75@gmail.com"),
                license = @License(name = "Apache foundation", url = "hhtps://apache.org")
        ),
        security = {
                @SecurityRequirement(name = "Bearer")
        }

)
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "Bearer"
)
public class OpenAPIConfig {
}
