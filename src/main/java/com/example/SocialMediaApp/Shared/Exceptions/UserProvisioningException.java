package com.example.SocialMediaApp.Shared.Exceptions;

import lombok.Getter;

@Getter
public class UserProvisioningException extends RuntimeException {

    public UserProvisioningException(String message){
        super(message);
    }
}
