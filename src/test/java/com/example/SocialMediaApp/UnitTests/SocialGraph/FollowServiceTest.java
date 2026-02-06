package com.example.SocialMediaApp.UnitTests.SocialGraph;

import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.Profile.api.dto.profileDetails;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.Shared.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.application.FollowRequestService;
import com.example.SocialMediaApp.SocialGraph.application.FollowService;
import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import com.example.SocialMediaApp.SocialGraph.domain.events.followAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.followRemoved;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FollowServiceTest {

    @Mock
    private FollowRepo followRepo;
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private BlocksRepo blocksRepo;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private FollowCacheUpdater followCacheUpdater;
    @InjectMocks
    private FollowRequestService followRequestService;
    @InjectMocks
    private FollowService followService;

    private final User currentuser=new User(UUID.randomUUID().toString());
    private final User requesteduser = new User(UUID.randomUUID().toString());

    @BeforeEach
    public  void setCurrentUser() {
       when(authenticatedUserService.getcurrentuser()).thenReturn(currentuser);
    }

    // FOLLOW CREATION TESTS
    @Nested
    @DisplayName("Follow Creation Tests")
    class FollowCreationTests {

        @Test
        public void Follow_yourself_throwsBadFollowException(){
            Exception exception=assertThrows(BadFollowRequestException.class,
                    ()->followService.Follow(currentuser.getId()));
            assertEquals("you cant follow yourself", exception.getMessage());
        }

        @Test
        public void follow_alreadyFollowedUser_throwsBadFollowException() {
            when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, requesteduser, Follow.Status.ACCEPTED))
                    .thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getId()), "Already followed");
        }

        @Test
        public void follow_pendingFollowRequest_throwsBadFollowRequestException() {
          lenient().when(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, requesteduser, Follow.Status.PENDING))
                    .thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getId()), "request already sent");
        }

        @Test
        public void follow_userBlockedByCurrentUser_throwsBadFollowRequestException() {
            lenient().when(blocksRepo.existsByBlockerAndBlocked(currentuser,requesteduser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getId()), "You cant follow this User");
        }

        @Test
        public void follow_userHasBlockedCurrentUser_throwsBadFollowRequestException() {
            when(blocksRepo.existsByBlockerAndBlocked(requesteduser,currentuser)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(requesteduser.getId()), "You cant follow this User");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true,false})
        public void follow_public_privateProfiles(boolean Public){
            when(profileRepo.existsByUserAndIsprivateFalse(requesteduser)).thenReturn(Public);
            profileDetails profileDetails=followService.Follow(requesteduser.getId());
            assertEquals(requesteduser.getId(),profileDetails.getUserId());
            ArgumentCaptor<Follow> captor1=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor1.capture());
            Follow follow=captor1.getValue();
            assertEquals(follow.getFollower().getId(),currentuser.getId());
            assertEquals(follow.getFollowing().getId(),requesteduser.getId());
            ArgumentCaptor<FollowNotification> captor2=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor2.capture());
            FollowNotification followNotification=captor2.getValue();
            assertEquals(followNotification.getTrigger().getId(),currentuser.getId());
            assertEquals(followNotification.getRecipient().getId(),requesteduser.getId());
            if(Public){
                assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
               assertEquals(FollowNotification.notificationType.FOLLOW,followNotification.getType());
               assertEquals(RelationshipStatus.FOLLOWING, profileDetails.getStatus());
               verify(eventPublisher).publishEvent(any(followAdded.class));
               ArgumentCaptor<FollowQueryHelper.Position> captor3=ArgumentCaptor.forClass(FollowQueryHelper.Position.class);
                ArgumentCaptor<FollowCacheUpdater.UpdateType> captor4=ArgumentCaptor.forClass(FollowCacheUpdater.UpdateType.class);
                ArgumentCaptor<String> captor5=ArgumentCaptor.forClass(String.class);
                verify(followCacheUpdater).UpdateCount(captor3.capture(),captor5.capture(),captor4.capture());
                //assertEquals();

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
            assertThrows(NoRelationShipException.class,()->followService.UnFollow(requesteduser.getId()),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void unfollow_pending_accepted_followings(Follow.Status status){
            Follow follow =new Follow(currentuser,requesteduser);
            follow.setStatus(status);
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.of(follow));
            followService.UnFollow(requesteduser.getId());
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                verify(eventPublisher).publishEvent(any(followRemoved.class));
            }
        }


        @Test
        public void removefollower_notfollower_throwsNoRelationShipException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.empty());
            assertThrows(NoRelationShipException.class,()->followService.removefollower(requesteduser.getId()),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void removefollower_pending_accepted_followers(Follow.Status status){
            Follow follow=new Follow(requesteduser,currentuser);
            follow.setStatus(status);
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.of(follow));
            followService.removefollower(requesteduser.getId());
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                ArgumentCaptor<followRemoved> captor=ArgumentCaptor.forClass(followRemoved.class);
                verify(eventPublisher).publishEvent(captor.capture());
                followRemoved followRemoved=captor.getValue();
                assertThat(follow).usingRecursiveComparison().isEqualTo(followRemoved.getFollow());

            }else{
                ArgumentCaptor<FollowNotification> captor=ArgumentCaptor.forClass(FollowNotification.class);
                verify(eventPublisher).publishEvent(captor.capture());
                FollowNotification followNotification=captor.getValue();
                assertEquals(FollowNotification.notificationType.FOLLOWING_REJECTED,followNotification.getType());
                assertEquals(requesteduser.getId(),followNotification.getRecipient().getId());
                assertEquals(currentuser.getId(),followNotification.getTrigger().getId());
            }
        }

    }

    @Nested
    @Description("Follow request Tests")
    class FollowRequestTests{

       // ACCEPT FOLLOW METHOD TESTS
       @Test
       public void accept_not_requested_throwsBadFollowRequestException(){
           when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.empty());
           assertthrows(NoRelationShipException.class,()->followRequestService.acceptFollow(requesteduser.getId()),"No relation with user found");
       }

        @Test
        public void acceptAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(requesteduser.getId()),"couldn't perform accept follow action on this user");
        }

        @Test
        public void accept_Follow_save_update_cache(){

            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.PENDING)));

            assertDoesNotThrow(()->followRequestService.acceptFollow(requesteduser.getId()));
            ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow=captor.getValue();
            assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
            assertEquals(follow.getFollower().getId(),requesteduser.getId());
            assertEquals(follow.getFollowing().getId(),currentuser.getId());

            ArgumentCaptor<Object> captor1=ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher,times(2)).publishEvent(captor1.capture());
          captor1.getAllValues().forEach(o->{
              if(o instanceof FollowNotification followNotification){
                  assertEquals(FollowNotification.notificationType.FOLLOWING_ACCEPTED,followNotification.getType());
                  assertEquals(requesteduser.getId(),followNotification.getRecipient().getId());
                  assertEquals(currentuser.getId(),followNotification.getTrigger().getId());
              } else if (o instanceof followAdded followAdded){
                  assertThat(follow).usingRecursiveComparison().isEqualTo(followAdded.getFollow());
              }
          });

        }


        // REJECT FOLLOW METHOD TESTS

        @Test
        public void reject_not_requested_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.empty());
            assertthrows(NoRelationShipException.class,()->followRequestService.rejectFollow(requesteduser.getId()),"No relation with user found");
        }

        @Test
        public void rejectAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.rejectFollow(requesteduser.getId()),"couldn't perform reject follow action on this user");
        }

        @Test
        public void reject_Follow_save_update_cache(){

            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.PENDING)));
            assertDoesNotThrow(()->followRequestService.rejectFollow(requesteduser.getId()));
            verify(followRepo).delete(any(Follow.class));
            ArgumentCaptor<FollowNotification> captor=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor.capture());
            FollowNotification notification=captor.getValue();
            assertEquals(FollowNotification.notificationType.FOLLOWING_REJECTED, notification.getType());
            assertEquals(notification.getTrigger(),currentuser);
            assertEquals(notification.getRecipient(),requesteduser);
        }

        // UNSEND FOLLOWING REQUESTS

        @Test
        public void unsend_not_requested_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.empty());
            assertthrows(NoRelationShipException.class,()->followRequestService.unsendFollowingRequest(requesteduser.getId()),"No relation with user found");
        }

        @Test
        public void unsendAlreadyFollowing(){
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.of(new Follow(currentuser,requesteduser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.unsendFollowingRequest(requesteduser.getId()),"couldn't perform unsend follow request on this user");
        }

        @Test
        public void UnsendFollowSuccess_removeFollow(){
           Follow follow=new Follow(currentuser,requesteduser, Follow.Status.PENDING);
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).
                    thenReturn(Optional.of(follow));
            assertDoesNotThrow(()->followRequestService.unsendFollowingRequest(requesteduser.getId()));
            verify(followRepo).delete(follow);
        }

    }


    // Helper method for asserting exceptions
    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage) {
        Exception exception = assertThrows(expected, executable);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
