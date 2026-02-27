package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Exceptions.WebhookSignatureException;
import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.WebSocketHandshakeException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookVerification {

    @Value("${supabase.webhook.secret}")
    private String webhookSecret;
    private final UploadValidationService uploadValidationService;

    public void verifySignature(String signature) {
        if (signature == null || !signature.equals(webhookSecret)) {

            throw new WebhookSignatureException("Invalid webhook signature");
        }
    }

    public void verifyFileUploaded(String filepath, SupabaseWebhookPayload.StorageRecord storageRecord){
        Map<String,Object> metaData=storageRecord.getMetadata();
        String fileMimeType = (String) metaData.get("mimetype");
        Long fileSize = ((Number) metaData.get("size")).longValue();
        UploadType uploadType=UploadType.valueOf(filepath.split("/")[1].toUpperCase());
        uploadValidationService.validateFile(new UploadRequest(fileMimeType,fileSize,uploadType));
    }

}
