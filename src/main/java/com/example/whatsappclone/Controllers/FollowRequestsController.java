package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Services.RelationShipsServices.FollowRequestService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/requests")
@RequiredArgsConstructor
public class FollowRequestsController {

    private final FollowRequestService followRequestService;


    @PutMapping("/me/{followid}/accept")
    public void acceptfollow(@PathVariable String followid) {
        followRequestService.acceptFollow(followid);
    }

    @DeleteMapping("/me/{followid}/reject")
    public void rejectfollow(@PathVariable String followid) {
        followRequestService.rejectFollow(followid);
    }

    @PutMapping("/me/{followid}/unsend")
    public void unsendrequest(@PathVariable String followid) {
        followRequestService.unsendFollowingRequest(followid);
    }
}

