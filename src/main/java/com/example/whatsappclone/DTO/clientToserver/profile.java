package com.example.whatsappclone.DTO.clientToserver;

import com.example.whatsappclone.Entities.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
public class profile {

    @Size(max = 30)
    @JsonProperty("bio")
    private String bio;
    private String username;
}
