package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostManagementService {

    private final StorageService storageService;

}
