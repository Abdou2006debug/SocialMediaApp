package com.example.whatsappclone.User.application;
import com.example.whatsappclone.User.api.dto.userregistration;


public interface IdentityService {
    String UserProvision(userregistration userregistration);
    void UserRemoval(String userId);
    void changeUsername(String userId,String username);
}
