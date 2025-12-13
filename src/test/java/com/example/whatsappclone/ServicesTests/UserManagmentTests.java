package com.example.whatsappclone.ServicesTests;

import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Services.CachService;
import com.example.whatsappclone.Services.UsersManagmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserManagmentTests {
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private CachService cachService;
    @InjectMocks
    private UsersManagmentService usersManagmentService;


    // GET USER PROFILE TESTS

    private void helper(boolean incache, boolean cacheit) {
        Profile mockedProfile = incache ? new Profile() : null;
        User mockedUser = new User();


        when(cachService.getcachedprofile(mockedUser)).thenReturn(mockedProfile);

        Profile repoProfile = new Profile();
        if (!incache) {
            when(profileRepo.findByUser(mockedUser)).thenReturn(Optional.of(repoProfile));
        }

        usersManagmentService.getuserprofile(mockedUser, cacheit);


        if (incache) {
            verify(profileRepo, never()).findByUser(mockedUser);
            verify(cachService, never()).cachuserprofile(any());
        } else if (cacheit) {
            verify(profileRepo).findByUser(mockedUser);
            verify(cachService).cachuserprofile(repoProfile);
        } else {
            verify(profileRepo).findByUser(mockedUser);
            verify(cachService, never()).cachuserprofile(any());
        }
    }
    @ParameterizedTest
    @CsvSource({
            "true, true",// Profile in cache
            "false, true",// Profile not in cache and cache
            "false, false"// Profile not in cache and dont cache
    })
    public void GetUserProfile(boolean incache,boolean cacheit){
        helper(incache,cacheit);
    }

}