package com.example.whatsappclone.User.application;


import com.example.whatsappclone.User.api.dto.userregistration;
import com.example.whatsappclone.Shared.Exceptions.UserProvisioningException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakRegistrationService implements IdentityService {

    // injecting keycloak details from properties
    @Value("${keycloak.username}")
    private String username;
    @Value("${keycloak.password}")
    private String password;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // method responsible for creating the user record inside the identity provider
    public void UserProvision(userregistration userregistration,String userId){
        Keycloak keycloak= KeycloakBuilder.builder().
                realm("master").username(username).password(password).
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms"))).clientId("admin-cli").build();
        String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);
        RealmResource realm=keycloak.realm(realmName);
        org.keycloak.representations.idm.UserRepresentation userRepresentation= new org.keycloak.representations.idm.UserRepresentation();
        userRepresentation.setEmail(userregistration.getEmail());
        userRepresentation.setUsername(userregistration.getUsername());
        userRepresentation.setFirstName(userRepresentation.getFirstName());
        userRepresentation.setLastName(userRepresentation.getLastName());
        userRepresentation.setEmailVerified(true);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userregistration.getPassword());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setEnabled(true);
        Map<String,List<String>> attributes=new HashMap<>();
        // this the most important attribute responsible for linking the jwt token with the user in the db
        attributes.put("userId",List.of(userId));
        userRepresentation.setAttributes(attributes);
        try(Response response= realm.users().create(userRepresentation)){
            if(response.getStatus()!=201){
                throw new UserProvisioningException();
            }
        }
    }

}
