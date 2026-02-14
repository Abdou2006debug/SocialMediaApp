package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.application.PostManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/post")
public class PostManagementController {

    private final PostManagementService postManagementService;




}
