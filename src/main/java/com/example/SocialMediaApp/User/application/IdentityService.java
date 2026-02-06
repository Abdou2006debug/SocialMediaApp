package com.example.SocialMediaApp.User.application;
import com.example.SocialMediaApp.User.api.dto.userregistration;


public interface IdentityService {
    String UserProvision(userregistration userregistration);
    void UserRemoval(String userId);
    void changeUsername(String userId,String username);
}
