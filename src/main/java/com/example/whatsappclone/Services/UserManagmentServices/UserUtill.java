package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.DTO.clientToserver.userregistration;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.UserProvisioningException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserUtill {
    @Value("${keycloak.username}")
    private String username;
    @Value("${keycloak.password}")
    private String password;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${supabase.url}")
    private String supabaseUri;
    @Value("${file.bucket}")
    private String bucket;
    @Value("${supabase.key}")
    private String apikey;

    private final WebClient webClient;
    public UserUtill(WebClient.Builder webclient){
        this.webClient= webclient.baseUrl(supabaseUri).build();
    }
    public void UserProvision(userregistration userregistration,String userId){
        Keycloak keycloak= KeycloakBuilder.builder().
                realm("master").username(username).password(password).
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms"))).clientId("admin-cli").build();
        String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);
        RealmResource realm=keycloak.realm(realmName);
        org.keycloak.representations.idm.UserRepresentation userRepresentation= new org.keycloak.representations.idm.UserRepresentation();
        userRepresentation.setEmail(userregistration.getEmail());
        userRepresentation.setUsername(userregistration.getUsername());
        userRepresentation.setEmailVerified(true);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userregistration.getPassword());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setEnabled(true);
        Map<String,List<String>> attributes=new HashMap<>();
        attributes.put("userId",List.of(userId));
        userRepresentation.setAttributes(attributes);
        try(Response response= realm.users().create(userRepresentation)){
            if(response.getStatus()!=201){
                throw new UserProvisioningException();
            }
        }
    }
    //public void uploadpfp(MultipartFile file,String oldpfp){
      //  webClient.post().header("Authorization","Bearer "+apikey).header("apikey", apikey)
    //}
}
