package com.example.whatsappclone.Shared.Exceptions;

import lombok.Getter;

import java.util.Map;
@Getter
public class UserProvisioningException extends RuntimeException {

    public UserProvisioningException(String message){
        super(message);
    }
}
