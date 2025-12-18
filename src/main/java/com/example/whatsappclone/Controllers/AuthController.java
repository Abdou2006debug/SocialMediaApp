package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.Services.UsersAccountManagmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsersAccountManagmentService usersManagment;

    @PostMapping("/register")
    public void register(@RequestBody @Valid userregistration user) {
        usersManagment.registeruser(user);
    }
}
