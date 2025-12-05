package com.example.whatsappclone.Exceptions;

public class BadFollowRequestException extends RuntimeException {
    public BadFollowRequestException(String message) {
        super(message);
    }
}
