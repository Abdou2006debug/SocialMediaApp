package com.example.SocialMediaApp.SocialGraph.api;

import com.example.SocialMediaApp.Profile.api.dto.profileDetails;
import com.example.SocialMediaApp.SocialGraph.application.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public profileDetails follow(@PathVariable String userId) {
        return followService.Follow(userId);
    }

    @DeleteMapping("/{userId}/unfollow")
    public void unfollow(@PathVariable String userId) {
        followService.UnFollow(userId);
    }


    @DeleteMapping("/{userId}/removefollower")
    public void removefollower(@PathVariable String userId) {
        followService.removefollower(userId);
    }


}

