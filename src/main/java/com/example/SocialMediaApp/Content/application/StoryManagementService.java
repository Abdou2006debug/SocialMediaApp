package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryManagementService {

    private final StorageService storageService;


}
