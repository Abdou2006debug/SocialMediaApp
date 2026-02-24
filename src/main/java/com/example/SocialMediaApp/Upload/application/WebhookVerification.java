package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebhookVerification {

    @Value("${supabase.webhook.secret}")
    private String webhookSecret;

    public void verify(String signature) {
        if (signature == null || !signature.equals(webhookSecret)) {

            throw new ActionNotAllowedException("Invalid webhook signature");
        }
    }
}
