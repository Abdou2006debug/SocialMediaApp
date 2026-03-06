package com.example.SocialMediaApp.Upload.Exceptions;

public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
