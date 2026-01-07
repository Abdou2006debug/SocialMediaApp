package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Services.RelationShipsServices.FollowQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/relationships")
public class FollowQueryController {
    private final FollowQueryService followQueryService;

    @GetMapping("/{userid}/followers/{page}")
    public List<profileSummary> getuserfollowers(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.listUserFollowers(userid, page);
    }

    @GetMapping("/{userid}/followings/{page}")
    public List<profileSummary> getuserfollowings(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.listUserFollowing(userid, page);
    }

    @GetMapping("/me/followers/requested/{page}")
    public List<profileSummary> getfollowrequests(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowRequests(page);
    }

    @GetMapping("/me/followings/requested/{page}")
    public List<profileSummary> getfollowingrequests(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowingRequests(page);
    }

    @GetMapping("/me/followers/accepted/{page}")
    public List<profileSummary> getfollowers(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowers(page);
    }

    @GetMapping("/me/followings/accepted/{page}")
    public List<profileSummary> getfollowings(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowings(page);
    }
}

