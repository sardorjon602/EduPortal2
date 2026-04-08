package sfera.eduportal2.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sfera.eduportal2.Payload.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> notFoundException(NotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ApiResponse(ex.getMessage(), HttpStatus.NOT_FOUND,false,null)
        );
    }
}
