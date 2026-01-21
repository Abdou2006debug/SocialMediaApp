package com.example.whatsappclone.UnitTests.SocialGraph;

import com.example.whatsappclone.Notification.domain.events.FollowNotification;
import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Shared.Exceptions.NoRelationShipException;
import com.example.whatsappclone.SocialGraph.application.FollowRequestService;
import com.example.whatsappclone.SocialGraph.application.FollowService;
import com.example.whatsappclone.SocialGraph.application.cache.FollowCacheWriter;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.RelationshipStatus;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.domain.events.followRemoved;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.domain.User;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;
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
    @InjectMocks
    private FollowRequestService followRequestService;
    @InjectMocks
    private FollowService followService;

    private final User currentuser=new User(UUID.randomUUID().toString());
    private final User requesteduser = new User(UUID.randomUUID().toString());

    @BeforeEach
    public  void setCurrentUser() {
       when(authenticatedUserService.getcurrentuser(any(boolean.class))).thenReturn(currentuser);
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
            assertEquals(follow.getFollower().getUuid(),currentuser.getUuid());
            assertEquals(follow.getFollowing().getUuid(),requesteduser.getUuid());
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
            Follow follow =new Follow(currentuser,requesteduser);
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
            Follow follow=new Follow(requesteduser,currentuser);
            follow.setStatus(status);
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.of(follow));
            followService.removefollower(requesteduser.getUuid());
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
                assertEquals(requesteduser.getUuid(),followNotification.getRecipient().getUuid());
                assertEquals(currentuser.getUuid(),followNotification.getTrigger().getUuid());
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
           assertthrows(NoRelationShipException.class,()->followRequestService.acceptFollow(requesteduser.getUuid()),"No relation with user found");
       }

        @Test
        public void acceptAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(requesteduser.getUuid()),"couldn't perform accept follow action on this user");
        }

        @Test
        public void accept_Follow_save_update_cache(){

            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.PENDING)));

            assertDoesNotThrow(()->followRequestService.acceptFollow(requesteduser.getUuid()));
            ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow=captor.getValue();
            assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
            assertEquals(follow.getFollower().getUuid(),requesteduser.getUuid());
            assertEquals(follow.getFollowing().getUuid(),currentuser.getUuid());

            ArgumentCaptor<Object> captor1=ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher,times(2)).publishEvent(captor1.capture());
          captor1.getAllValues().forEach(o->{
              if(o instanceof FollowNotification followNotification){
                  assertEquals(FollowNotification.notificationType.FOLLOWING_ACCEPTED,followNotification.getType());
                  assertEquals(requesteduser.getUuid(),followNotification.getRecipient().getUuid());
                  assertEquals(currentuser.getUuid(),followNotification.getTrigger().getUuid());
              } else if (o instanceof followAdded followAdded){
                  assertThat(follow).usingRecursiveComparison().isEqualTo(followAdded.getFollow());
              }
          });

        }


        // REJECT FOLLOW METHOD TESTS

        @Test
        public void reject_not_requested_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.empty());
            assertthrows(NoRelationShipException.class,()->followRequestService.rejectFollow(requesteduser.getUuid()),"No relation with user found");
        }

        @Test
        public void rejectAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.rejectFollow(requesteduser.getUuid()),"couldn't perform reject follow action on this user");
        }

        @Test
        public void reject_Follow_save_update_cache(){

            when(followRepo.findByFollowerAndFollowing(requesteduser,currentuser)).
                    thenReturn(Optional.of(new Follow(requesteduser,currentuser, Follow.Status.PENDING)));
            assertDoesNotThrow(()->followRequestService.rejectFollow(requesteduser.getUuid()));
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
            assertthrows(NoRelationShipException.class,()->followRequestService.unsendFollowingRequest(requesteduser.getUuid()),"No relation with user found");
        }

        @Test
        public void unsendAlreadyFollowing(){
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).thenReturn(Optional.of(new Follow(currentuser,requesteduser, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.unsendFollowingRequest(requesteduser.getUuid()),"couldn't perform unsend follow request on this user");
        }

        @Test
        public void UnsendFollowSuccess_removeFollow(){
           Follow follow=new Follow(currentuser,requesteduser, Follow.Status.PENDING);
            when(followRepo.findByFollowerAndFollowing(currentuser,requesteduser)).
                    thenReturn(Optional.of(follow));
            assertDoesNotThrow(()->followRequestService.unsendFollowingRequest(requesteduser.getUuid()));
            verify(followRepo).delete(follow);
        }

    }


    // Helper method for asserting exceptions
    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage) {
        Exception exception = assertThrows(expected, executable);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
