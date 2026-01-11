package com.example.whatsappclone.Profile.api;

import com.example.whatsappclone.Profile.api.dto.profile;
import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.application.ProfileQueryService;
import com.example.whatsappclone.Profile.application.ProfileUpdatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users/me/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileQueryService profileQueryService;
    private final ProfileUpdatingService profileUpdatingService;

    @GetMapping
    public profileDetails getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileQueryService.getUserProfile(jwt.getClaimAsString("userId"));
    }

    @PostMapping
    public void updateProfile(@RequestBody @Valid profile profile) {
        profileUpdatingService.UpdateProfile(profile);
    }

    @PutMapping("/picture")
    public void updateProfilePicture(@RequestParam MultipartFile file) throws IOException {
        profileUpdatingService.changeProfileAvatar(file);
    }

    @GetMapping("/settings")
    public profilesettings getProfileSettings() {
        return profileQueryService.getMyProfileSettings();
    }

    @PutMapping("/settings")
    public void updateProfileSettings(@RequestBody profilesettings settings) {
        profileUpdatingService.UpdateProfileSettings(settings);
    }
}

