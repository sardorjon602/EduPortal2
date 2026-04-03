package sfera.eduportal2.Payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqQuestions {
    @Schema(hidden = true)
    private Long id;

    @NotBlank(message = "Dont leave blank spaces")
    private String text;

}
