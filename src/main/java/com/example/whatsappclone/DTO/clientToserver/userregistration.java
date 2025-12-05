package com.example.whatsappclone.DTO.clientToserver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class userregistration {
    @Size(min = 6,max = 10)
    @NotNull
    private String firstname;
    @Size(min = 6,max = 10)
    @NotNull
    private String lastname;
    @Size(min = 6,max = 10)
    @NotNull
    private String username;
    @Email
    private String email;
    @NotNull
    private String password;
    private LocalDate birthday;
}
