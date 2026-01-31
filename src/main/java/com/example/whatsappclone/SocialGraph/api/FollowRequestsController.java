package com.example.whatsappclone.SocialGraph.api;

import com.example.whatsappclone.SocialGraph.application.FollowRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowRequestsController {

    private final FollowRequestService followRequestService;


    @PutMapping("/{userId}/accept")
    public void acceptfollow(@PathVariable String userId) {
        followRequestService.acceptFollow(userId);
    }

    @DeleteMapping("/{userId}/reject")
    public void rejectfollow(@PathVariable String userId) {
        followRequestService.rejectFollow(userId);
    }

    @DeleteMapping("/{userId}/unsend")
    public void unsendrequest(@PathVariable String userId) {
        followRequestService.unsendFollowingRequest(userId);
    }
}

