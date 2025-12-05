package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.user;

import com.example.whatsappclone.Services.BlockService;

import com.example.whatsappclone.Services.FollowRequestService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/followrequests")
@RequiredArgsConstructor
public class FollowRequestsController {

    private final FollowRequestService followRequestService;

    @GetMapping("/me/{page}")
    public List<user> getfollowrequests(@PathVariable int page) {
        return followRequestService.ListFollowRequests(page);
    }

    @GetMapping("/me/following/{page}")
    public List<user> getfollowingrequests(@PathVariable int page) {
        return followRequestService.listafollowingrequests(page);
    }

    @PutMapping("/me/{followid}/accept")
    public void acceptfollow(@PathVariable String followid) {
        followRequestService.acceptfollow(followid);
    }

    @DeleteMapping("/me/{followid}/reject")
    public void rejectfollow(@PathVariable String followid) {
        followRequestService.rejectfollow(followid);
    }

    @PutMapping("/me/{followid}/unsend")
    public void unsendrequest(@PathVariable String followid) {
        followRequestService.unsendfollowingrequest(followid);
    }
}

