package sfera.eduportal2.entity.Payload;

import lombok.*;
import org.springframework.http.HttpStatus;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    private String message;

    private HttpStatus status;

    private boolean success;

    private Object body;

}
