package com.example.SocialMediaApp.Shared.Exceptions;

public class ActionNotAllowedException extends RuntimeException {
    public ActionNotAllowedException(String message) {
        super(message);
    }
}
