package com.example.whatsappclone.UnitTests;

import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import com.example.whatsappclone.Services.UserManagmentServices.UsersAccountManagmentService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserManagmentTests {
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private CacheWriterService cachService;
    @Mock
    private UserQueryService userQueryService;
    @InjectMocks
    private UsersAccountManagmentService usersManagmentService;


    // GET USER PROFILE TESTS

    private void helper(boolean incache, boolean cacheit) {
        Profile mockedProfile = incache ? new Profile() : null;
        User mockedUser = new User();


        when(cachService.getcachedprofile(mockedUser)).thenReturn(mockedProfile);

        Profile repoProfile = new Profile();
        if (!incache) {
            when(profileRepo.findByUser(mockedUser)).thenReturn(Optional.of(repoProfile));
        }

        userQueryService.getuserprofile(mockedUser, cacheit);


        if (incache) {
            verify(profileRepo, never()).findByUser(mockedUser);
            verify(cachService, never()).cacheUserProfile(any());
        } else if (cacheit) {
            verify(profileRepo).findByUser(mockedUser);
            verify(cachService).cacheUserProfile(repoProfile);
        } else {
            verify(profileRepo).findByUser(mockedUser);
            verify(cachService, never()).cacheUserProfile(any());
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