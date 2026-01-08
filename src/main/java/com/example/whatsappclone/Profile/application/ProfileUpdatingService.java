package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.DTO.clientToserver.profile;
import com.example.whatsappclone.DTO.clientToserver.profilesettings;
import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;@
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "storage.bucket")
public class ProfileUpdatingService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileRepo profileRepo;
    private final CacheWriterService cacheWriterService;
    private final RestTemplate restTemplate;
    private final UserRepo userRepo;
    private final ProfileQueryService profileQueryService;

    private String apikey;
    private String profileAvatars;

    public void UpdateProfileSettings(profilesettings profilesettings){
        User currentuser= authenticatedUserService.getcurrentuser(false);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);
        currentprofile.setIsprivate(profilesettings.isIsprivate());
        currentprofile.setShowifonline(profilesettings.isShowifonline());
        profileRepo.save(currentprofile);
        cacheWriterService.cacheUserProfile(currentprofile);
    }

    public void changepfp(MultipartFile file) throws IOException {
        User currentuser= authenticatedUserService.getcurrentuser(false);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);
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
        String supabaseUrl="https://mrzxtjddhikbcjccejce.supabase.co";
        String uploadUrl = supabaseUrl + "/storage/v1/object/" +profileAvatars+ "/" + fileName;
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
                profileAvatars +"/"+ URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        profileRepo.save(currentprofile);
        cacheWriterService.cacheUserProfile(currentprofile);
    }
    public void UpdateProfile(profile p){
        User currentuser= authenticatedUserService.getcurrentuser(true);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        currentprofile.setUsername(p.getUsername());
        userRepo.save(currentuser);
        profileRepo.save(currentprofile);
        cacheWriterService.cacheUserProfile(currentprofile);
    }
}
