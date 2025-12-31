package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.profileDetails;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserQueryService userQueryService;

    @GetMapping("/{userid}/account")
    public profileDetails getaccount(@PathVariable String userid) {
        return userQueryService.getuser(userid);
    }
}

