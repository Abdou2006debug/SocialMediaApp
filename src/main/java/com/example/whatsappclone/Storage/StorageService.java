package com.example.whatsappclone.Storage;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface StorageService {
    String uploadAvatartoStorage(MultipartFile file, String uri) throws IOException;
}
