package com.example.whatsappclone.UnitTests;

import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Exceptions.NoRelationShipException;
import com.example.whatsappclone.SocialGraph.application.cache.FollowCacheWriter;
import com.example.whatsappclone.SocialGraph.domain.RelationshipStatus;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.domain.events.followRemoved;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Notification.domain.events.FollowNotification;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.SocialGraph.application.FollowRequestService;
import com.example.whatsappclone.SocialGraph.application.FollowService;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FollowServiceTest {

    @Mock
    private FollowRepo followRepo;
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private BlocksRepo blocksRepo;
    @Mock
    private FollowCacheWriter followCacheWriter;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private FollowRequestService followRequestService;
    @InjectMocks
    private FollowService followService;

    private final User currentuser=new User(UUID.randomUUID().toString());
    private final User requesteduser = new User(UUID.randomUUID().toString());
    private final Follow follow =new Follow(currentuser,requesteduser);

    @BeforeAll
    public  void setCurrentUser() {
       when(authenticatedUserService.getcurrentuser(false)).thenReturn(currentuser);
    }

    // FOLLOW CREATION TESTS
    @Nested
    @DisplayName("Follow Creation Tests")
    class FollowCreationTests {

        @Test
        public void Follow_yourself_throwsBadFollowException(){
            Exception exception=assertThrows(BadFollowRequestException.class,
                    ()->followService.Follow(currentuser.getUuid()));
            assertEquals("you cant follow yourself", exception.getMessage());
        }

        @Test
        public void follow_alreadyFollowedUser_throwsBadFollowException() {
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
            lenient().when(blocksRepo.existsByBlockerAndBlocked(currentuser,requesteduser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "You cant follow this User");
        }

        @Test
        public void follow_userHasBlockedCurrentUser_throwsBadFollowRequestException() {
            when(blocksRepo.existsByBlockerAndBlocked(requesteduser,currentuser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getUuid()), "You cant follow this User");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true,false})
        public void follow_public_privateProfiles(boolean Public){
            when(profileRepo.existsByUserAndIsprivateFalse(requesteduser)).thenReturn(Public);
            profileDetails profileDetails=followService.Follow(requesteduser.getUuid());
            assertEquals(requesteduser.getUuid(),profileDetails.getUserId());
            ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow=captor.getValue();
            assertEquals(follow.getFollower_id(),currentuser.getUuid());
            assertEquals(follow.getFollowing_id(),requesteduser.getUuid());
            ArgumentCaptor<FollowNotification> captor2=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor2.capture());
            FollowNotification followNotification=captor2.getValue();
            assertEquals(followNotification.getTrigger().getUuid(),currentuser.getUuid());
            assertEquals(followNotification.getRecipient().getUuid(),requesteduser.getUuid());
            if(Public){
                assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
               assertEquals(FollowNotification.notificationType.FOLLOW,followNotification.getType());
               assertEquals(RelationshipStatus.FOLLOWING, profileDetails.getStatus());
               verify(eventPublisher).publishEvent(any(followAdded.class));

            }else{
                assertEquals(Follow.Status.PENDING,follow.getStatus());
                assertEquals(FollowNotification.notificationType.FOLLOW_REQUESTED,followNotification.getType());
                assertEquals(RelationshipStatus.FOLLOW_REQUESTED,profileDetails.getStatus());
            }
        }

    }

    // FOLLOW REMOVAL TESTS
    @Nested
    @DisplayName("Follow Removal Tests")
    class FollowRemovalTests {
        @Test
        public void unfollow_notfollowing_throwsNoRelationShipException(){
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.empty());
            assertThrows(NoRelationShipException.class,()->followService.UnFollow(requesteduser.getUuid()),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void unfollow_pending_accepted_followings(Follow.Status status){
            follow.setStatus(status);
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.of(follow));
            followService.UnFollow(requesteduser.getUuid());
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                verify(eventPublisher).publishEvent(any(followRemoved.class));
            }
        }


        @Test
        public void removefollower_notfollower_throwsNoRelationShipException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.empty());
            assertThrows(NoRelationShipException.class,()->followService.removefollower(requesteduser.getUuid()),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void removefollower_pending_accepted_followers(Follow.Status status){
            Follow follow1=new Follow(requesteduser,currentuser);
            follow1.setStatus(status);
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.of(follow1));
            followService.removefollower(requesteduser.getUuid());
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                verify(eventPublisher).publishEvent(any(followRemoved.class));
            }else{

            }
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
