package com.example.SocialMediaApp.Storage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


public class SignRequest {
    private int expiresIn;
    public SignRequest(int expiresIn){
        this.expiresIn=60*expiresIn;
    }
}
