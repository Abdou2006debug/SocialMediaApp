package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.user;

import com.example.whatsappclone.Services.FollowUtill;

import com.example.whatsappclone.Services.FollowQueryService;
import com.example.whatsappclone.Services.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final FollowQueryService followQueryService;
    @PostMapping("/me/{userid}")
    public user follow(@PathVariable String userid) {
        return followService.Follow(userid);
    }

    @DeleteMapping("/me/{followid}")
    public void unfollow(@PathVariable String followid) {
        followService.UnFollow(followid);
    }

    @GetMapping("/me/followers/{page}")
    public List<user> getfollowers(@PathVariable int page) {
        return followQueryService.ListMyfollowers(page);
    }

    @GetMapping("/me/followings/{page}")
    public List<user> getfollowings(@PathVariable int page) {
        return followQueryService.listMyfollowings(page);
    }

    @DeleteMapping("/me/{followid}/removefollower")
    public void removefollower(@PathVariable String followid) {
        followService.removefollower(followid);
    }

    @GetMapping("/{userid}/followers/{page}")
    public List<user> getuserfollowers(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.getUserFollow(userid, page, FollowUtill.Position.FOLLOWER);
    }

    @GetMapping("/{userid}/followings/{page}")
    public List<user> getuserfollowings(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.getUserFollow(userid, page, FollowUtill.Position.FOLLOWING);
    }
}

