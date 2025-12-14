package com.example.whatsappclone.UnitTests.ServicesTests;

import com.example.whatsappclone.DTO.clientToserver.profilesettings;
import com.example.whatsappclone.DTO.serverToclient.user;
import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {
    @Mock
    private  FollowRepo followRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private UsersManagmentService usersManagment;
    @Mock
    private BlocksRepo blocksRepo;
    @Mock
    private CachService cachService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private FollowUtill followHelperService;

    @InjectMocks
    private FollowRequestService followRequestService;
    @InjectMocks
    private FollowService followService;

    private final User currentuser =new User("abdoumimi",UUID.randomUUID().toString());
    private final User requesteduser=new User("abdou",UUID.randomUUID().toString());
    private final String followUUID=UUID.randomUUID().toString();
    @BeforeEach
    public void SetCurrentUser(){
    when(usersManagment.getcurrentuser()).thenReturn(currentuser);
      lenient().when(userRepo.findById(requesteduser.getUuid())).thenReturn(Optional.of(requesteduser));
    }


    //FOLLOW METHOD TESTS

    @Nested
@DisplayName("Follow logic test behaviour for most cases")
class followTesting{
    @Test
    public void FollowYourself(){
        Exception exception=assertThrows(BadFollowRequestException.class,()->followService.Follow(currentuser.getUuid()));
        assertEquals("you cant follow yourself", exception.getMessage());
    }
    @Test
    public void UserNotFound(){
        when(userRepo.findById(requesteduser.getUuid())).thenReturn(Optional.empty());
        Exception exception=assertThrows(UserNotFoundException.class,()->followService.Follow(requesteduser.getUuid()));
        assertEquals("User not found", exception.getMessage());
    }
    @Test
    public void UserAlreadyFollowed(){
        when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.ACCEPTED)).thenReturn(true);
        assertthrows(BadFollowRequestException.class,
                ()->followService.Follow(requesteduser.getUuid()),"Already followed");
    }
    @Test
    public void CurrentUserBlocked(){
        when(blocksRepo.existsByBlockedAndBlocker(currentuser,requesteduser)).thenReturn(true);
        assertthrows(BadFollowRequestException.class,
                ()->followService.Follow(requesteduser.getUuid()),"User has blocked you");
    }
    @Test
    public void CurrentUserBlocker(){
        lenient().when(blocksRepo.existsByBlockedAndBlocker(requesteduser,currentuser)).thenReturn(true);
        assertthrows(BadFollowRequestException.class,
                ()->followService.Follow(requesteduser.getUuid()),"You have blocked this user");
    }
    @Test
    public void RequestAlreadySent(){
        lenient().when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.PENDING)).thenReturn(true);
        assertthrows(BadFollowRequestException.class, ()->followService.Follow(requesteduser.getUuid()),"request already sent");
    }
    @Test
    public void FollowSuccessPublicProfile(){
        when(profileRepo.findByUser(requesteduser)).thenReturn(
                Optional.of(Profile.builder().user(requesteduser).bio("test").isprivate(false).build()));
        user user=followService.Follow(requesteduser.getUuid());
        ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
        verify(followRepo).save(captor.capture());
        Follow follow=captor.getValue();
        assertEquals(follow.getFollower(),currentuser);
        assertEquals(follow.getFollowing(),requesteduser);
        assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
        ArgumentCaptor<notification> captor1=ArgumentCaptor.forClass(notification.class);
        verify(eventPublisher).publishEvent(captor1.capture());
        notification notification=captor1.getValue();
        assertEquals(notification.getTrigger(),currentuser);
        assertEquals(notification.getRecipient(),requesteduser);
        assertEquals(notification.getFollowid(),follow.getUuid());
        assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOW);
        verify(cachService).addfollowing(currentuser,follow);
        verify(cachService).addfollower(requesteduser,follow);
        assertEquals(user.getUseruuid(),requesteduser.getUuid());
        assertEquals(user.getUsername(),requesteduser.getUsername());
    }
    @Test
    public void FollowSuccessPrivateProfile(){
        when(profileRepo.findByUser(requesteduser)).thenReturn(
                Optional.of(Profile.builder().user(requesteduser).bio("test").isprivate(true).build()));
        user user= followService.Follow(requesteduser.getUuid());
        ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
        verify(followRepo).save(captor.capture());
        Follow follow=captor.getValue();
        assertEquals(follow.getFollower(),currentuser);
        assertEquals(follow.getFollowing(),requesteduser);
        assertEquals(Follow.Status.PENDING, follow.getStatus());
        ArgumentCaptor<notification> captor1=ArgumentCaptor.forClass(notification.class);
        verify(eventPublisher).publishEvent(captor1.capture());
        notification notification=captor1.getValue();
        assertEquals(notification.getTrigger(),currentuser);
        assertEquals(notification.getRecipient(),requesteduser);
        assertEquals(notification.getFollowid(),follow.getUuid());
        assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOW_REQUESTED);
        verify(cachService,never()).addfollowing(currentuser,follow);
        verify(cachService,never()).addfollower(requesteduser,follow);
        assertEquals(user.getUseruuid(),requesteduser.getUuid());
        assertEquals(user.getUsername(),requesteduser.getUsername());
    }
}



    //REMOVE FOLLOWER METHOD TESTS
    @Nested
    @DisplayName("unfollow and remove from followers test behaviour for most cases")
    public class removefollowTest{
        @Test
        public void RemoveFollowerDontExist(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,
                    ()->followService.removefollower(followUUID),"bad request");
        }
        @Test
        public void RemovePendingFollower(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.PENDING)));
            assertthrows(BadFollowRequestException.class,
                    ()->followService.removefollower(followUUID),"user not in followers try to reject the request");
        }
        @Test
        public void RemoveFollower(){
            Follow Mockedfollow =new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED);
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
            assertDoesNotThrow(()->followService.removefollower(followUUID));
            verify(followRepo).delete(Mockedfollow);
            verify(cachService).removefollower(currentuser, Mockedfollow);
            verify(cachService).removefollowing(requesteduser, Mockedfollow);
        }


        //UNFOLLOW METHOD TESTS
        @Test
        public void UnfollowDontExist(){
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()->followService.UnFollow(followUUID),"bad request");
        }
        @Test
        public void UnfollowPending(){
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(new Follow(currentuser,requesteduser, Follow.Status.PENDING)));
            assertthrows(BadFollowRequestException.class,
                    ()->followService.UnFollow(followUUID),"you are not following this user try to unsend the request");
        }
        @Test
        public void UnfollowSuccess(){
            Follow Mockedfollow =new Follow(currentuser,requesteduser,Follow.Status.ACCEPTED);
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
            assertDoesNotThrow(()->followService.UnFollow(followUUID));
            verify(followRepo).delete(Mockedfollow);
            verify(cachService).removefollower(requesteduser, Mockedfollow);
            verify(cachService).removefollowing(currentuser, Mockedfollow);
        }
    }
    public static void assertthrows(Class<? extends Exception> expected,Executable executable,String expectedMessage){
        Exception exception=assertThrows(expected,executable);
        assertEquals(expectedMessage,exception.getMessage());
    }

@Nested
@DisplayName("follower and following request test behaviour for most cases")
public class followRequestsHandlingTests{
        @Test
        public void AcceptFollowerDontExist(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptfollow(followUUID),"bad request");
        }
    @Test
    public void AcceptAlreadyFollower(){
        when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
        assertthrows(BadFollowRequestException.class,
                ()->followRequestService.acceptfollow(followUUID),"user already follow you");
    }
    @Test
    public void AcceptFollowSuccess(){
        Follow Mockedfollow=new Follow(requesteduser,currentuser, Follow.Status.PENDING);
        when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
        assertDoesNotThrow(()->followRequestService.acceptfollow(followUUID));
        ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
        verify(followRepo).save(captor.capture());
        Follow follow=captor.getValue();
        assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
        ArgumentCaptor<notification> captor1=ArgumentCaptor.forClass(notification.class);
        verify(eventPublisher).publishEvent(captor1.capture());
        notification notification=captor1.getValue();
        assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_ACCEPTED);
        assertEquals(notification.getTrigger(),currentuser);
        assertEquals(notification.getRecipient(),requesteduser);
        verify(cachService).addfollower(currentuser,follow);
        verify(cachService).addfollowing(requesteduser,follow);
    }


    // REJECT FOLLOW METHOD TESTS
    @Test
    public void RejectFollowerDontExist(){
        when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.empty());
        assertthrows(BadFollowRequestException.class,()->followRequestService.acceptfollow(followUUID),"bad request");
    }
    @Test
    public void RejectAlreadyFollower(){
        when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
        assertthrows(BadFollowRequestException.class,
                ()->followRequestService.rejectfollow(followUUID),"User Already in followers");
    }
    @Test
    public void RejectFollowSuccess(){
        Follow follow=new Follow(requesteduser,currentuser, Follow.Status.PENDING);
        when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(follow));
        assertDoesNotThrow(()->followRequestService.rejectfollow(followUUID));
        verify(followRepo).delete(follow);
        ArgumentCaptor<notification> captor=ArgumentCaptor.forClass(notification.class);
        verify(eventPublisher).publishEvent(captor.capture());
        notification notification=captor.getValue();
        assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_REJECTED);
        assertEquals(notification.getTrigger(),currentuser);
        assertEquals(notification.getRecipient(),requesteduser);
    }




    // UNSEND FOLLOWING REQUEST TESTS
    @Test
    public void UnsendFollowingDontExist(){
        when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.empty());
        assertthrows(BadFollowRequestException.class,()->followRequestService.unsendfollowingrequest(followUUID),"bad request");
    }
    @Test
    public void UnsendAlreadyFollowing(){
        when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(new Follow(currentuser,requesteduser, Follow.Status.ACCEPTED)));
        assertthrows(BadFollowRequestException.class,
                ()->followRequestService.unsendfollowingrequest(followUUID),"you already follow this user");
    }
    @Test
    public void UnsendFollowSuccess(){
        Follow Mockedfollow=new Follow(currentuser,requesteduser, Follow.Status.PENDING);
        when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
        assertDoesNotThrow(()->followRequestService.unsendfollowingrequest(followUUID));
        verify(followRepo).delete(Mockedfollow);
    }

}


// UPDATE PROFILE TEST
    private void helper(boolean iscurrentprivate, boolean toprivate, boolean trigger){
        Profile Mockedprofile=new Profile(iscurrentprivate);
        profilesettings MockedprofileSettings=new profilesettings(toprivate);
        when(usersManagment.getuserprofile(currentuser,true)).thenReturn(Mockedprofile);
        followRequestService.UpdateProfileSettings(MockedprofileSettings);
        ArgumentCaptor<Profile> captor=ArgumentCaptor.forClass(Profile.class);
        verify(profileRepo).save(captor.capture());
        Profile profile=captor.getValue();
        assertEquals(MockedprofileSettings.isIsprivate(),profile.isIsprivate());
        verify(cachService).cachuserprofile(profile);
        if(trigger){
            verify(followRepo).findByFollowingAndStatus(currentuser, Follow.Status.PENDING);
        }else{
            verify(followRepo,never()).findByFollowingAndStatus(currentuser, Follow.Status.PENDING);
        }
    }
    @ParameterizedTest
    @CsvSource({
            "true, false,true",// private to public should trigger
            "false,true,false",// public to private shouldnt trigger
            "true, true,false",// private to private // //     //
            "false, false,false", // public to public // //    //
    })
    public void UpdateProfile(boolean iscurrentprivate,boolean toprivate,boolean shouldtrigger){
        helper(iscurrentprivate,toprivate,shouldtrigger);
    }
}
