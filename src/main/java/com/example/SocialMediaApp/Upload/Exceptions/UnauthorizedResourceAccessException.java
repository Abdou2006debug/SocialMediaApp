package com.example.SocialMediaApp.Upload.Exceptions;

public class UnauthorizedResourceAccessException extends RuntimeException {
    public UnauthorizedResourceAccessException(String message) {
        super(message);
    }
}
