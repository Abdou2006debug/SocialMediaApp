package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Services.RelationShipsServices.FollowQueryService;
import com.example.whatsappclone.Services.RelationShipsServices.FollowService;
import com.example.whatsappclone.Services.RelationShipsServices.UserFollowViewHelper;
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
    public List<profileSummary> getfollowers(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowers(page);
    }


    @GetMapping("/me/followings/{page}")
    public List<profileSummary> getfollowings(@PathVariable int page) {
        return followQueryService.listCurrentUserFollowings(page);
    }

    @DeleteMapping("/me/{followid}/removefollower")
    public void removefollower(@PathVariable String followid) {
        followService.removefollower(followid);
    }

    @GetMapping("/{userid}/followers/{page}")
    public List<profileSummary> getuserfollowers(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.listUserFollowers(userid, page);
    }

    @GetMapping("/{userid}/followings/{page}")
    public List<profileSummary> getuserfollowings(@PathVariable String userid, @PathVariable int page) {
        return followQueryService.listUserFollowing(userid, page);
    }
}

