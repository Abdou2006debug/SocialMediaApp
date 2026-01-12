package com.example.whatsappclone.User.application;

import com.example.whatsappclone.User.api.dto.userregistration;

public interface IdentityService {
    void UserProvision(userregistration userregistration,String userId);
}
