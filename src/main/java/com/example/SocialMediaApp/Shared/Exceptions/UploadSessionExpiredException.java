package com.example.SocialMediaApp.Shared.Exceptions;

public class UploadSessionExpiredException extends RuntimeException {
    public UploadSessionExpiredException(String message) {
        super(message);
    }
}
