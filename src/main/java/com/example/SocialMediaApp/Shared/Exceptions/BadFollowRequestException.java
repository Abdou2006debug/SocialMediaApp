package com.example.SocialMediaApp.Shared.Exceptions;

public class BadFollowRequestException extends RuntimeException {
    public BadFollowRequestException(String message) {
        super(message);
    }
}
