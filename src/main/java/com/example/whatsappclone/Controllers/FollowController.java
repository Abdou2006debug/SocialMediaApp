package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Services.RelationShipsServices.FollowQueryService;
import com.example.whatsappclone.Services.RelationShipsServices.FollowService;
import com.example.whatsappclone.Services.RelationShipsServices.UserFollowViewHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/followactions")
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



    @DeleteMapping("/me/{followid}/removefollower")
    public void removefollower(@PathVariable String followid) {
        followService.removefollower(followid);
    }


}

