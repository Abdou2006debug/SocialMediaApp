package com.example.SocialMediaApp.Shared.Exceptions;

public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
