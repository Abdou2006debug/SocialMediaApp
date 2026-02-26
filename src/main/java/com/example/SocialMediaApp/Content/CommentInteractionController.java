package com.example.SocialMediaApp.Content;

import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/comments")
public class CommentInteractionController {

    private final CommentInteractionController commentInteractionController;

}
