package sfera.eduportal2.Exception;

import sfera.eduportal2.Payload.ApiResponse;


public class    NotFoundException extends RuntimeException {
    public NotFoundException(ApiResponse message) {
        super(message.getMessage());
    }
}
