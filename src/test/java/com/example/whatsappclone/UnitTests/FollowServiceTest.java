package com.example.whatsappclone.UnitTests;

import com.example.whatsappclone.User.domain.cache.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Notification.domain.events.FollowNotification;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import com.example.whatsappclone.SocialGraph.application.FollowRequestService;
import com.example.whatsappclone.SocialGraph.application.FollowService;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
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
    private FollowRepo followRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private BlocksRepo blocksRepo;
    @Mock
    private CacheWriterService cachService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserQueryService userQueryService;
    @InjectMocks
    private FollowRequestService followRequestService;
    @InjectMocks
    private FollowService followService;

    private final User currentuser = new User("abdoumimi", UUID.randomUUID().toString());
    private final User requesteduser = new User("abdou", UUID.randomUUID().toString());
    private final String followUUID = UUID.randomUUID().toString();

    @BeforeEach
    public void setCurrentUser() {
        when(userQueryService.getcurrentuser()).thenReturn(currentuser);
        lenient().when(userRepo.findById(requesteduser.getUuid())).thenReturn(Optional.of(requesteduser));
    }

    // FOLLOW CREATION TESTS
    @Nested
    @DisplayName("Follow Creation Tests")
    class FollowCreationTests {
        @Test
        public void FollowYourself(){
            Exception exception=assertThrows(BadFollowRequestException.class,
                    ()->followService.Follow(currentuser.getUuid()));
            assertEquals("you cant follow yourself", exception.getMessage());
        }
        @Test
        public void follow_nonExistingUser_throwsUserNotFoundException() {
            assertthrows(UserNotFoundException.class,
                    () -> followService.Follow("test"), "User not found");
        }

        @Test
        public void follow_alreadyFollowedUser_throwsBadFollowRequestException() {
            when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, requesteduser, Follow.Status.ACCEPTED))
                    .thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "Already followed");
        }

        @Test
        public void follow_pendingFollowRequest_throwsBadFollowRequestException() {
           lenient().when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, requesteduser, Follow.Status.PENDING))
                    .thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "request already sent");
        }

        @Test
        public void follow_userBlockedByCurrentUser_throwsBadFollowRequestException() {
            lenient().when(blocksRepo.existsByBlockedAndBlocker(requesteduser, currentuser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "You have blocked this user");
        }

        @Test
        public void follow_userHasBlockedCurrentUser_throwsBadFollowRequestException() {
            when(blocksRepo.existsByBlockedAndBlocker(currentuser, requesteduser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "User has blocked you");
        }

        @Test
        public void follow_publicProfile_savesFollowAndUpdatesCache() {
            when(profileRepo.findByUser(requesteduser))
                    .thenReturn(Optional.of(Profile.builder().user(requesteduser).bio("test").isprivate(false).build()));
            user user = followService.Follow(requesteduser.getUuid());

            ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow = captor.getValue();
            assertEquals(follow.getStatus(), Follow.Status.ACCEPTED);


        }

        @Test
        public void follow_privateProfile_savesPendingRequest() {
            when(profileRepo.findByUser(requesteduser))
                    .thenReturn(Optional.of(Profile.builder().user(requesteduser).bio("test").isprivate(true).build()));
            user user = followService.Follow(requesteduser.getUuid());

            ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow = captor.getValue();
            assertEquals(follow.getStatus(), Follow.Status.PENDING);

            verify(cachService, never()).addfollowing(currentuser, follow);
            verify(cachService, never()).addfollower(requesteduser, follow);
        }
    }

    // FOLLOW REMOVAL TESTS
    @Nested
    @DisplayName("Follow Removal Tests")
    class FollowRemovalTests {
        @Test
        public void unfollow_DontExist(){
            when(followRepo.findByUuidAndFollower(followUUID, currentuser))
                    .thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()-> followService.UnFollow(followUUID),"bad request");
        }
        @Test
        public void unfollow_pendingFollow_throwsBadFollowRequestException() {
            when(followRepo.findByUuidAndFollower(followUUID, currentuser))
                    .thenReturn(Optional.of(new Follow(currentuser, requesteduser, Follow.Status.PENDING)));
            assertthrows(BadFollowRequestException.class,
                    () -> followService.UnFollow(followUUID), "you are not following this user try to unsend the request");
        }

        @Test
        public void unfollow_acceptedFollow_deletesFollowAndClearsCache() {
            Follow follow = new Follow(currentuser, requesteduser, Follow.Status.ACCEPTED);
            when(followRepo.findByUuidAndFollower(followUUID, currentuser)).thenReturn(Optional.of(follow));

            assertDoesNotThrow(() -> followService.UnFollow(followUUID));
            verify(followRepo).delete(follow);
            verify(cachService).removefollowing(currentuser, follow);
            verify(cachService).removefollower(requesteduser, follow);
        }
        @Test
        public void removeFollower_DontExist(){
            when(followRepo.findByUuidAndFollowing(followUUID, currentuser))
                    .thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()-> followService.removefollower(followUUID),"bad request");
        }
        @Test
        public void removeFollower_pendingFollow_throwsBadFollowRequestException() {
            when(followRepo.findByUuidAndFollowing(followUUID, currentuser))
                    .thenReturn(Optional.of(new Follow(requesteduser, currentuser, Follow.Status.PENDING)));
            assertthrows(BadFollowRequestException.class,
                    () -> followService.removefollower(followUUID), "user not in followers try to reject the request");
        }

        @Test
        public void removeFollower_acceptedFollow_deletesFollowAndClearsCache() {
            Follow follow = new Follow(requesteduser, currentuser, Follow.Status.ACCEPTED);
            when(followRepo.findByUuidAndFollowing(followUUID, currentuser)).thenReturn(Optional.of(follow));

            assertDoesNotThrow(() -> followService.removefollower(followUUID));
            verify(followRepo).delete(follow);
            verify(cachService).removefollowing(requesteduser, follow);
            verify(cachService).removefollower(currentuser, follow);
        }
    }
    @Nested
    class FollowRequestTests{
        @Test
        public void acceptFollowerDontExist(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(followUUID),"bad request");
        }
        @Test
        public void acceptAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(followUUID),"user already follow you");
        }
        @Test
        public void acceptFollowSuccess_savesFollowAndSendNotiAndUpdadesCache(){
            Follow Mockedfollow=new Follow(requesteduser,currentuser, Follow.Status.PENDING);
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
            assertDoesNotThrow(()->followRequestService.acceptFollow(followUUID));
            ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow=captor.getValue();
            assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
            ArgumentCaptor<FollowNotification> captor1=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor1.capture());
            FollowNotification notification=captor1.getValue();
            assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_ACCEPTED);
            assertEquals(notification.getTrigger(),currentuser);
            assertEquals(notification.getRecipient(),requesteduser);
            verify(cachService).addfollower(currentuser,follow);
            verify(cachService).addfollowing(requesteduser,follow);
        }


        // REJECT FOLLOW METHOD TESTS
        @Test
        public void rejectFollowerDontExist(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(followUUID),"bad request");
        }
        @Test
        public void rejectAlreadyFollower_throwsBadFollowRequestExecption(){
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.rejectFollow(followUUID),"User Already in followers");
        }
        @Test
        public void rejectFollowSuccess_removeFollowAndUpdatesCache(){
            Follow follow=new Follow(requesteduser,currentuser, Follow.Status.PENDING);
            when(followRepo.findByUuidAndFollowing(followUUID,currentuser)).thenReturn(Optional.of(follow));
            assertDoesNotThrow(()->followRequestService.rejectFollow(followUUID));
            verify(followRepo).delete(follow);
            ArgumentCaptor<FollowNotification> captor=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor.capture());
            FollowNotification notification=captor.getValue();
            assertEquals(notification.getType(), com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_REJECTED);
            assertEquals(notification.getTrigger(),currentuser);
            assertEquals(notification.getRecipient(),requesteduser);
        }

        // UNSEND FOLLOWING REQUESTS
        @Test
        public void unsendFollowingDontExist(){
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.empty());
            assertthrows(BadFollowRequestException.class,()->followRequestService.unsendFollowingRequest(followUUID),"bad request");
        }
        @Test
        public void unsendAlreadyFollowing(){
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(new Follow(currentuser,requesteduser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.unsendFollowingRequest(followUUID),"you already follow this user");
        }
        @Test
        public void UnsendFollowSuccess_removeFollow(){
            Follow Mockedfollow=new Follow(currentuser,requesteduser, Follow.Status.PENDING);
            when(followRepo.findByUuidAndFollower(followUUID,currentuser)).thenReturn(Optional.of(Mockedfollow));
            assertDoesNotThrow(()->followRequestService.unsendFollowingRequest(followUUID));
            verify(followRepo).delete(Mockedfollow);
        }
    }
    private void helper(boolean iscurrentprivate, boolean toprivate, boolean trigger){
        Profile Mockedprofile=new Profile(iscurrentprivate);
        profilesettings MockedprofileSettings=new profilesettings(toprivate);
        when(userQueryService.getuserprofile(currentuser,true)).thenReturn(Mockedprofile);
        followRequestService.UpdateProfileSettings(MockedprofileSettings);
        ArgumentCaptor<Profile> captor=ArgumentCaptor.forClass(Profile.class);
        verify(profileRepo).save(captor.capture());
        Profile profile=captor.getValue();
        assertEquals(MockedprofileSettings.isIsprivate(),profile.isIsprivate());
        verify(cachService).cacheUserProfile(profile);
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

    // Helper method for asserting exceptions
    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage) {
        Exception exception = assertThrows(expected, executable);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
