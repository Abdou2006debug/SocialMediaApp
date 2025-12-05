package com.example.whatsappclone.Services;

import com.example.whatsappclone.DTO.clientToserver.notificationsettings;
import com.example.whatsappclone.DTO.clientToserver.profile;
import com.example.whatsappclone.DTO.clientToserver.profilesettings;
import com.example.whatsappclone.DTO.serverToclient.account;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

//import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class UsersManagmentService {
       private final CachService cachService;
       private final NotificationSettingsRepo notificationSettingsRepo;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final RestTemplate restTemplate;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;
    @Value("${supabase_key}")
    private String apikey;
    public User getcurrentuser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
            return null;
        }
           String keycloakid=((Jwt) authentication.getPrincipal()).getSubject();
            User cachedtuser=cachService.getUserbyKeycloakId(keycloakid);
            if(cachedtuser!=null){
                return cachedtuser;
            }
          User user=  userRepo.findByKeycloakId(keycloakid).
                  orElseThrow(()->new UserNotFoundException("user not found"));
            cachService.cachuser(user);
          return user;
        }
    public void registeruser(userregistration userregistration){
        Keycloak keycloak= KeycloakBuilder.builder().realm("master").
                clientId("admin-cli").username("admin").password("Admin123!").
                serverUrl("http://keycloak:8080").build();
        RealmResource realm=keycloak.realm("Realm");
        org.keycloak.representations.idm.UserRepresentation userRepresentation= new org.keycloak.representations.idm.UserRepresentation();
        userRepresentation.setEmail(userregistration.getEmail());
        userRepresentation.setUsername(userregistration.getUsername());
        userRepresentation.setFirstName(userregistration.getFirstname());
        userRepresentation.setLastName(userregistration.getLastname());
        userRepresentation.setEmailVerified(false);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userregistration.getPassword());
       userRepresentation.setCredentials(List.of(credentialRepresentation));
       userRepresentation.setEnabled(true);
        Response response= realm.users().create(userRepresentation);
       if(response.getStatus()==201){
         String keycloakid= CreatedResponseUtil.getCreatedId(response);

         User user=new User(userregistration.getUsername(),userregistration.getFirstname(),
                 userregistration.getLastname(),userregistration.getEmail(),keycloakid);

           userRepo.save(user);

           Profile profile=new Profile(null,userregistration.getUsername());
           profile.setUser(user);
           NotificationsSettings notificationsSettings=new NotificationsSettings(true,true,true);
           notificationsSettings.setUser(user);
           notificationSettingsRepo.save(notificationsSettings);
           profileRepo.save(profile);
           cachService.cachuser(user);
       }

    }

public void updateNotificationSettings(notificationsettings notification){
        User currentuser=getcurrentuser();
       NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(currentuser);
       notificationsSettings.setOnfollow(notification.isOnfollower());
       notificationsSettings.setOnfollowingrequest_rejected(notification.isOnfollowingrequests_rejected());
       notificationsSettings.setOnfollowingrequest_Accepted(notification.isOnfollowingrequests_accepted());
     notificationSettingsRepo.save(notificationsSettings);
}
public notificationsettings getnotificationsettings(){
        User currentuser=getcurrentuser();
       NotificationsSettings notificationsSettings= notificationSettingsRepo.findByUser(currentuser);
       return new notificationsettings(notificationsSettings.getOnfollowingrequest_Accepted(),notificationsSettings.getOnfollowingrequest_rejected(),notificationsSettings.getOnfollow());

}
    public void uploadpfp(MultipartFile file) throws IOException {
        User currentuser=getcurrentuser();
        Profile cachedprofile=cachService.getcachedprofile(currentuser);
        Profile currentprofile=cachedprofile==null?
                profileRepo.findByUser(currentuser).get():cachedprofile;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " +apikey);
        headers.set("apikey", apikey);
        String prepfp=currentprofile.getPrivateavatarurl();
        if(prepfp!=null){
            try{
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    prepfp,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );
            }catch(Exception e){

            }
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String bucketname="pfpfiles";
        String supabaseUrl="https://mrzxtjddhikbcjccejce.supabase.co";
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketname+ "/" + fileName;
        headers.set("x-upsert", "true");
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }
        currentprofile.setPrivateavatarurl(uploadUrl);
        currentprofile.setPublicavatarurl(supabaseUrl+ "/storage/v1/object/public/" +
                bucketname +"/"+URLEncoder.encode(fileName,StandardCharsets.UTF_8));
        profileRepo.save(currentprofile);
        cachService.cachuserprofile(currentprofile);
    }
    public void UpdateProfile(profile p){
        User currentuser=getcurrentuser();
        Profile cachedprofile=cachService.getcachedprofile(currentuser);
        Profile currentprofile=cachedprofile==null?
                profileRepo.findByUser(currentuser).get():cachedprofile;
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        profileRepo.save(currentprofile);
        cachService.cachuserprofile(currentprofile);
    }
    public com.example.whatsappclone.DTO.serverToclient.profile getMyProfile(){
        User currentuser=getcurrentuser();
        Profile cachedprofile=cachService.getcachedprofile(currentuser);
        Profile profile=cachedprofile==null?
                profileRepo.findByUser(currentuser).get():cachedprofile;
        com.example.whatsappclone.DTO.serverToclient.profile profiledto=
                new com.example.whatsappclone.DTO.serverToclient.profile();
        profiledto.setAvatarurl(profile.getPublicavatarurl());
        profiledto.setBio(profile.getBio());
        profiledto.setUseruuid(currentuser.getUuid());
        profiledto.setUsername(currentuser.getUsername());
        profiledto.setFollowings(followingsCount(currentuser));
        profiledto.setFollowers(followersCount(currentuser));
        return profiledto;
    }
    public com.example.whatsappclone.DTO.serverToclient.profilesettings getMyProfileSettings(){
        User currentuser=getcurrentuser();
        Profile cachedprofile=cachService.getcachedprofile(currentuser);
        Profile profile=cachedprofile==null?
                profileRepo.findByUser(currentuser).get():cachedprofile;
        return new com.example.whatsappclone.DTO.serverToclient.
                profilesettings(profile.isIsprivate(),profile.isShowifonline());
    }
    public account getuser(String useruuid){
        User currentuser=getcurrentuser();
        User requesteduser;
        requesteduser=cachService.getUserbyId(useruuid);
        if (requesteduser == null) {
            requesteduser= userRepo.findById(useruuid).
                    orElseThrow(() -> new UserNotFoundException("user not found"));
        }
       // Profile cached = cachService.getcachedprofile(requesteduser);
        Profile profile =profileRepo.findByUser(requesteduser).get();
                //cached == null ? profileRepo.findByUser(requesteduser).get() : cached;
        String status=null;

        boolean hasblocked= blocksRepo.
                existsByBlockedAndBlocker(requesteduser,currentuser);
        if(hasblocked){
            status="you have blocked him";
        }
        boolean isfolloweraccepted = followRepo.
                existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.ACCEPTED);
        if(isfolloweraccepted){
            status="following";
        }else{
            boolean isfollowerpending=followRepo.
                    existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.PENDING);
            if(isfollowerpending){
                status="sent";
            }
        }
        boolean i=followRepo.existsByFollowerAndFollowing(currentuser,requesteduser);
        boolean isfollowingaccepted=followRepo.
                existsByFollowerAndFollowingAndStatus(requesteduser,currentuser, Follow.Status.ACCEPTED);
        if(isfollowingaccepted&&!i){
            status="follow back";
        }
boolean isonline=cachService.getuserstatus(requesteduser.getUsername());
     String lastseen=isonline?null: cachService.getuserlastseen(requesteduser.getUsername());
        return new account(requesteduser.getUuid(),requesteduser.getUsername(),
                profile.getPublicavatarurl(),profile.getBio(),status,followersCount(requesteduser),followingsCount(requesteduser),lastseen, isonline);
    }
    private long followersCount(User user){
        return followRepo.countByFollowingAndStatus(user, Follow.Status.ACCEPTED);

    }
    private long followingsCount(User user){
        return followRepo.countByFollowerAndStatus(user, Follow.Status.ACCEPTED);
    }
}

