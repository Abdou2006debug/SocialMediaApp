package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.clientToserver.notificationsettings;
import com.example.whatsappclone.DTO.clientToserver.profilesettings;
import com.example.whatsappclone.Services.FollowRequestService;
import com.example.whatsappclone.Services.UsersAccountManagmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UsersAccountManagmentService usersManagment;
    private final FollowRequestService followRequestsService;
    @PutMapping("/notifications")
    public void updateNotificationSettings(@RequestBody notificationsettings settings) {
        usersManagment.updateNotificationSettings(settings);
    }
@GetMapping("/notifications")
public notificationsettings getNotificationSettings(){
        return usersManagment.getnotificationsettings();
}
    @GetMapping("/profile")
    public com.example.whatsappclone.DTO.serverToclient.profilesettings getProfileSettings() {
        return usersManagment.getMyProfileSettings();
    }

    @PutMapping("/profile")
    public void updateProfileSettings(@RequestBody profilesettings settings) {
        followRequestsService.UpdateProfileSettings(settings);
    }
}

