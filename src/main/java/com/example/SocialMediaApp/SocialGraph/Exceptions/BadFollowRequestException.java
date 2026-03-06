package com.example.SocialMediaApp.SocialGraph.Exceptions;

public class BadFollowRequestException extends RuntimeException {
    public BadFollowRequestException(String message) {
        super(message);
    }
}
