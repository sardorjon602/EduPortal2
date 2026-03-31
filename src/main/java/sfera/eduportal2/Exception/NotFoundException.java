package sfera.eduportal2.Exception;

import sfera.eduportal2.entity.Payload.ApiResponse;

public class NotFoundException extends RuntimeException {
    public NotFoundException(ApiResponse message) {
        super(message.getMessage());
    }
}
