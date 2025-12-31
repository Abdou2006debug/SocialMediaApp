package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Services.UserManagmentServices.UsersAccountManagmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users/me/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UsersAccountManagmentService usersManagment;

    @GetMapping
    public profileSummary getMyProfile() {
        return usersManagment.getMyProfile();
    }

    @PostMapping
    public void updateProfile(@RequestBody @Valid com.example.whatsappclone.DTO.clientToserver.profile profile) {
        usersManagment.UpdateProfile(profile);
    }

    @PutMapping("/picture")
    public void updateProfilePicture(@RequestParam MultipartFile file) throws IOException {
        usersManagment.changepfp(file);
    }
}

