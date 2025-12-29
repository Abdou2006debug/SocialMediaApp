package com.example.whatsappclone.Services;

import com.example.whatsappclone.DTO.clientToserver.notificationsettings;
import com.example.whatsappclone.DTO.clientToserver.profile;
import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.Entities.User;
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
public class UsersAccountManagmentService {
    @Value("${keycloak.username}")
       private String username;
    @Value("${keycloak.password}")
       private String password;
       @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
       private String issuerUri;
       private final CachService cachService;
       private final NotificationSettingsRepo notificationSettingsRepo;
       private final UserRepo userRepo;
       private final ProfileRepo profileRepo;
       private final RestTemplate restTemplate;
      // private final WebClient webClient= WebClient.builder().baseUrl().build();
       private final UserQueryService userQueryService;
    @Value("${supabase.key}")
    private String apikey;
    public void registeruser(userregistration userregistration){
        Keycloak keycloak= KeycloakBuilder.builder().
                realm("master").username(username).password(password).
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms"))).clientId("admin-cli").build();
   String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);
        RealmResource realm=keycloak.realm(realmName);
        org.keycloak.representations.idm.UserRepresentation userRepresentation= new org.keycloak.representations.idm.UserRepresentation();
        userRepresentation.setEmail(userregistration.getEmail());
        userRepresentation.setUsername(userregistration.getUsername());
        userRepresentation.setFirstName(userregistration.getFirstname());
        userRepresentation.setLastName(userregistration.getLastname());
        userRepresentation.setEmailVerified(true);
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
           cachService.cacheUser(user);
       }else{
          throw new RuntimeException(response.readEntity(String.class));
       }
       response.close();
    }

public void updateNotificationSettings(notificationsettings notification){
        User currentuser=userQueryService.getcurrentuser();
       NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(currentuser);
       notificationsSettings.setOnfollow(notification.isOnfollower());
       notificationsSettings.setOnfollowingrequest_rejected(notification.isOnfollowingrequests_rejected());
       notificationsSettings.setOnfollowingrequest_Accepted(notification.isOnfollowingrequests_accepted());
     notificationSettingsRepo.save(notificationsSettings);
}
public notificationsettings getnotificationsettings(){
        User currentuser=userQueryService.getcurrentuser();
       NotificationsSettings notificationsSettings= notificationSettingsRepo.findByUser(currentuser);
       return new notificationsettings(notificationsSettings.getOnfollowingrequest_Accepted(),notificationsSettings.getOnfollowingrequest_rejected(),notificationsSettings.getOnfollow());

}
    public void uploadpfp(MultipartFile file) throws IOException {
        User currentuser=userQueryService.getcurrentuser();
Profile currentprofile=userQueryService.getuserprofile(currentuser,false);
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
        cachService.cacheUserProfile(currentprofile);
    }
    public void UpdateProfile(profile p){
        User currentuser=userQueryService.getcurrentuser();
        Profile currentprofile=userQueryService.getuserprofile(currentuser,true);
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        profileRepo.save(currentprofile);
        cachService.cacheUserProfile(currentprofile);
    }
    public com.example.whatsappclone.DTO.serverToclient.profile getMyProfile(){
        User currentuser=userQueryService.getcurrentuser();
        Profile profile=userQueryService.getuserprofile(currentuser,true);
        com.example.whatsappclone.DTO.serverToclient.profile profiledto=
                new com.example.whatsappclone.DTO.serverToclient.profile();
        profiledto.setAvatarurl(profile.getPublicavatarurl());
        profiledto.setBio(profile.getBio());
        profiledto.setUseruuid(currentuser.getUuid());
        profiledto.setUsername(currentuser.getUsername());
        profiledto.setFollowings(userQueryService.followingsCount(currentuser));
        profiledto.setFollowers(userQueryService.followersCount(currentuser));
        return profiledto;
    }
    public com.example.whatsappclone.DTO.serverToclient.profilesettings getMyProfileSettings(){
        User currentuser=userQueryService.getcurrentuser();
        Profile profile=userQueryService.getuserprofile(currentuser,true);
        return new com.example.whatsappclone.DTO.serverToclient.
                profilesettings(profile.isIsprivate(),profile.isShowifonline());
    }


}

