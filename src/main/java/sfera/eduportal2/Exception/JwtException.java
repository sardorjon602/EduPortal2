package sfera.eduportal2.Exception;

import sfera.eduportal2.entity.Payload.ApiResponse;

public class JwtException extends RuntimeException {
    public JwtException( String message) {
        super(message);
    }
}
