package com.example.SocialMediaApp.Shared.Exceptions;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }
}
