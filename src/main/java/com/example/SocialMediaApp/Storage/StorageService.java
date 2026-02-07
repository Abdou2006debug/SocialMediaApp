package com.example.SocialMediaApp.Storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String uploadAvatartoStorage(MultipartFile file, String uri) throws IOException;
}
