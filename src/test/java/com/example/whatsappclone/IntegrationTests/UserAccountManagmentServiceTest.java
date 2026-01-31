package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import com.example.whatsappclone.Notification.persistence.NotificationSettingsRepo;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.User.api.dto.userregistration;
import com.example.whatsappclone.User.application.RegistrationService;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserAccountManagmentServiceTest extends TestContainerConfig{

    private final RegistrationService registrationService;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final JwtDecoder jwtDecoder;

    private String createuser(){
        String username= UUID.randomUUID().toString().substring(0,7);
        userregistration userregistration=
                new userregistration("test","test",username,username+"@gmail.com","test", LocalDate.now());
        registrationService.registerUser(userregistration);
        return username;
    }

    @Test
    public void duplicateUser_throwsRuntimeException(){
        String username=createuser();
        userregistration userregistration=
                new userregistration("test","test",username
                        ,"test@gmail.com","test", LocalDate.now());
        assertThrows(RuntimeException.class, ()->registrationService.registerUser(userregistration));
    }

    @Test
    public void registeruser() throws IOException {
        String username=createuser();
        Optional<User>  user=userRepo.findByUsername(username);
        assertTrue(user.isPresent());
        String userId=user.get().getUuid();
        assertTrue(profileRepo.existsByUserUsername(username));
        Optional<NotificationsSettings> notificationsSettings=notificationSettingsRepo.findByUserUsername(username);
        assertTrue(notificationsSettings.isPresent());
        NotificationsSettings notificationsSettings1=notificationsSettings.get();
        assertTrue(notificationsSettings1.getOnfollow());
        assertTrue(notificationsSettings1.getOnfollowingrequestAccepted());
        assertTrue(notificationsSettings1.getOnfollowingrequestRejected());
        //checking if saved in keycloak
        Keycloak keycloakTest=KeycloakBuilder.builder().
                realm("master").username("admin").password("Admin123!").
                serverUrl("http://localhost:8080").clientId("admin-cli").build();
        assertEquals(1,keycloakTest.realm("Realm").users().searchByUsername(username,true).size());

       // testing jwt decoder with the registred user
        WebClient webClient=WebClient.builder().
                baseUrl("http://localhost:8080/realms/Realm/protocol/openid-connect/token").
                defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "app");
        form.add("username", username);
        form.add("password", "test");
        String response=webClient.post().body(BodyInserters.fromFormData(form)).retrieve().bodyToMono(String.class).block();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        String accessToken = node.get("access_token").asText();
        assertDoesNotThrow(()->jwtDecoder.decode(accessToken));
        assertEquals(jwtDecoder.decode(accessToken).getSubject(),userId);
    }

}
