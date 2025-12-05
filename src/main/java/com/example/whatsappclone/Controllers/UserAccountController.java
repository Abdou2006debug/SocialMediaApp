package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.serverToclient.account;
import com.example.whatsappclone.Services.UsersManagmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UsersManagmentService usersManagment;

    @GetMapping("/{userid}/account")
    public account getaccount(@PathVariable String userid) {
        return usersManagment.getuser(userid);
    }
}

