package com.example.whatsappclone.Identity.api;

import com.example.whatsappclone.Identity.api.dto.userregistration;
import com.example.whatsappclone.Identity.application.IdentityRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegistrationController {

    private final IdentityRegistrationService identityRegistrationService;

    @PostMapping("/register")
    public void register(@RequestBody @Valid userregistration user) {
        identityRegistrationService.registerUser(user);
    }

}
