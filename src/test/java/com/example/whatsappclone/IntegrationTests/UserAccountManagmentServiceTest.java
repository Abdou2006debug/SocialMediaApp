package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Repositries.NotificationSettingsRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
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
    private final RestTemplate restTemplate;
    @LocalServerPort
    private int port;
    private final UsersAccountManagmentService usersAccountManagmentService;
    private final UserRepo userRepo;
    private final JwtDecoder jwtDecoder;
    private final ProfileRepo profileRepo;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private String createuser(){
        String username= UUID.randomUUID().toString().substring(0,7);
        userregistration userregistration=
                new userregistration("test","test",username,username+"@gmail.com","test", LocalDate.now());
        usersAccountManagmentService.registeruser(userregistration);
        return username;
    }

    @Test
    public void duplicateUser_throwsRuntimeException(){
        String username=createuser();
        userregistration userregistration=
                new userregistration("test","test",username
                        ,"test@gmail.com","test", LocalDate.now());
        assertThrows(RuntimeException.class, ()->usersAccountManagmentService.registeruser(userregistration));

    }
    @Test
    public void registeruser() throws IOException {
        String username=createuser();
        assertTrue(userRepo.existsByUsername(username));
        assertTrue(profileRepo.existsByUserUsername(username));
        Optional<NotificationsSettings> notificationsSettings=notificationSettingsRepo.findByUserUsername(username);
        assertTrue(notificationsSettings.isPresent());
        NotificationsSettings notificationsSettings1=notificationsSettings.get();
        assertTrue(notificationsSettings1.getOnfollow());
        assertTrue(notificationsSettings1.getOnfollowingrequestAccepted());
        assertTrue(notificationsSettings1.getOnfollowingrequestRejected());
        //checking if saved in keycloak
        Keycloak keycloakTest=KeycloakBuilder.builder().
                realm("master").username("admin").password("admin").
                serverUrl("http://localhost:"+keycloak.getMappedPort(8080)).clientId("admin-cli").build();
        assertEquals(1,keycloakTest.realm("master").users().searchByUsername(username,true).size());
       // testing jwt decoder with the registred user
        HttpHeaders headers=new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", username);
        form.add("password", "test");
        var entity=new HttpEntity<>(form,headers);
        ResponseEntity<String> response=
                restTemplate.exchange("http://localhost:"+keycloak.getMappedPort(8080)+"/realms/master/protocol/openid-connect/token", HttpMethod.POST,entity,String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        String accessToken = node.get("access_token").asText();
        assertDoesNotThrow(()->jwtDecoder.decode(accessToken));
        assertEquals(jwtDecoder.decode(accessToken).getClaim("preferred_username"),username);

    }

}
