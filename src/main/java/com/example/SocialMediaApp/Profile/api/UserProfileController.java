package com.example.SocialMediaApp.Profile.api;

import com.example.SocialMediaApp.Profile.api.dto.profile;
import com.example.SocialMediaApp.Profile.api.dto.profileDetails;
import com.example.SocialMediaApp.Profile.api.dto.profilesettings;
import com.example.SocialMediaApp.Profile.application.ProfileQueryService;
import com.example.SocialMediaApp.Profile.application.ProfileUpdatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileQueryService profileQueryService;
    private final ProfileUpdatingService profileUpdatingService;

    @GetMapping("/me")
    public profileDetails getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileQueryService.getUserProfile(jwt.getSubject());
    }

    @GetMapping("/{userid}")
    public profileDetails getProfile(@PathVariable String userid) {
        return profileQueryService.getUserProfile(userid);
    }

    @PutMapping("/me")
    public void updateProfile(@RequestBody @Valid profile profile) {
        profileUpdatingService.UpdateProfile(profile);
    }

    @PutMapping("/me/picture")
    public void updateProfilePicture(@RequestParam MultipartFile file) throws IOException {
        profileUpdatingService.changeProfileAvatar(file);
    }

    @GetMapping("/me/settings")
    public profilesettings getProfileSettings() {
        return profileQueryService.getMyProfileSettings();
    }

    @PutMapping("/me/settings")
    public void updateProfileSettings(@RequestBody profilesettings settings) {
        profileUpdatingService.UpdateProfileSettings(settings);
    }
}

