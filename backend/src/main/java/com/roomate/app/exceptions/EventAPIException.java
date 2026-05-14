package com.roomate.app.exceptions;

public class EventAPIException extends RuntimeException {
    
    public EventAPIException(String message) {
        super(message);
    }
    
    public EventAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
