package sfera.eduportal2.Payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sfera.eduportal2.entity.Questions;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqOptions {
    @Schema(hidden = true)
    private Long id;

    @NotBlank(message = "Enter the right option")
    private boolean isCorrect;

    @NotBlank(message = "Enter text here")
    private String text;

    @NotBlank(message = "Enter to which question this options belong")
    @ManyToOne
    private Questions questions;
}
