package com.example.SocialMediaApp.SocialGraph.api;

import com.example.SocialMediaApp.Profile.api.dto.profileSummary;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryService;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/socialgraph")
@Validated
public class FollowQueryController {
    private final FollowQueryService followQueryService;

    @GetMapping("/{userid}/followers")
    public List<profileSummary> getuserFollowers(@PathVariable String userid,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int page) {
        return followQueryService.listUserFollowers(userid, page);
    }

    @GetMapping("/{userid}/followings")
    public List<profileSummary> getuserFollowings(@PathVariable String userid,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int page) {
        return followQueryService.listUserFollowing(userid, page);
    }

    @GetMapping("/me/followers")
    public List<profileSummary> getFollowers(@RequestParam(defaultValue = "ACCEPTED") Follow.Status status,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero  int page) {

        return status==Follow.Status.ACCEPTED?
                followQueryService.listCurrentUserFollowers(page):followQueryService.listCurrentUserFollowRequests(page);
    }

    @GetMapping("/me/followings")
    public List<profileSummary> getFollowings(@RequestParam(defaultValue = "ACCEPTED") Follow.Status status,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero  int page) {

        return status==Follow.Status.ACCEPTED?
                followQueryService.listCurrentUserFollowings(page):followQueryService.listCurrentUserFollowingRequests(page);
    }

}

