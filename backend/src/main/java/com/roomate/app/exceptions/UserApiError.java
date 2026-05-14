package com.roomate.app.exceptions;

public class UserApiError extends RuntimeException {
    public UserApiError(String message) {
        super(message);
    }

    public UserApiError() {
        super("Standard User Api Error");
    }
}
