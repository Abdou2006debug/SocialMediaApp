package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.DTO.clientToserver.notificationsettings;
import com.example.whatsappclone.DTO.clientToserver.profile;
import com.example.whatsappclone.DTO.clientToserver.profilesettings;
import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.DTO.serverToclient.profileDetails;
import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.NotificationSettingsRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class UsersAccountManagmentService {
       private final CacheWriterService cachService;
       private final NotificationSettingsRepo notificationSettingsRepo;
       private final UserRepo userRepo;
       private final ProfileRepo profileRepo;
       private final RestTemplate restTemplate;
       private final UserUtill userUtill;
       private final UserQueryService userQueryService;
    @Value("${supabase.key}")
    private String apikey;
    public void registeruser(userregistration userregistration){
         User user=userRepo.save(new User(userregistration.getUsername(),userregistration.getFirstname(),
                 userregistration.getLastname(),userregistration.getEmail()));
           Profile profile=new Profile(null,userregistration.getUsername());
           profile.setUser(user);
           NotificationsSettings notificationsSettings=new NotificationsSettings(true,true,true);
           notificationsSettings.setUser(user);
           notificationSettingsRepo.save(notificationsSettings);
           profileRepo.save(profile);
           cachService.cacheUser(user);
           userUtill.UserProvision(userregistration,user.getUuid());
    }
     public void updateNotificationSettings(notificationsettings notification){
        User currentuser=userQueryService.getcurrentuser(false);
        NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(currentuser);
        notificationsSettings.setOnfollow(notification.isOnfollower());
        notificationsSettings.setOnfollowingrequestRejected(notification.isOnfollowingrequests_rejected());
        notificationsSettings.setOnfollowingrequestAccepted(notification.isOnfollowingrequests_accepted());
        notificationSettingsRepo.save(notificationsSettings);
}
     public notificationsettings getnotificationsettings(){
       User currentuser=userQueryService.getcurrentuser(false);
       NotificationsSettings notificationsSettings= notificationSettingsRepo.findByUser(currentuser);
       return new notificationsettings(notificationsSettings.getOnfollowingrequestAccepted(),notificationsSettings.getOnfollowingrequestRejected(),notificationsSettings.getOnfollow());

}
    public void changepfp(MultipartFile file) throws IOException {
       User currentuser=userQueryService.getcurrentuser(false);
        Profile currentprofile=userQueryService.getuserprofile(currentuser,false);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bearer "+apikey);
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
        User currentuser=userQueryService.getcurrentuser(true);
        Profile currentprofile=userQueryService.getuserprofile(currentuser,false);
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        currentprofile.setUsername(p.getUsername());
        userRepo.save(currentuser);
        profileRepo.save(currentprofile);
        cachService.cacheUserProfile(currentprofile);
    }
    public profileDetails getMyProfile(){
        User currentuser=userQueryService.getcurrentuser(false);
        Profile profile=userQueryService.getuserprofile(currentuser,true);
       return null;
    }
    public com.example.whatsappclone.DTO.serverToclient.profilesettings getMyProfileSettings(){
     User currentuser=userQueryService.getcurrentuser(false);
        Profile profile=userQueryService.getuserprofile(currentuser,false);
        return new com.example.whatsappclone.DTO.serverToclient.
                profilesettings(profile.isIsprivate(),profile.isShowifonline());
    }
    public void UpdateProfileSettings(profilesettings profilesettings){
        User currentuser=userQueryService.getcurrentuser(false);
        Profile currentprofile=userQueryService.getuserprofile(currentuser,false);
        boolean currentstatus=currentprofile.isIsprivate();
        currentprofile.setIsprivate(profilesettings.isIsprivate());
        currentprofile.setShowifonline(profilesettings.isShowifonline());
        profileRepo.save(currentprofile);
        cachService.cacheUserProfile(currentprofile);
    }

}

