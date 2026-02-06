package com.example.SocialMediaApp.User.application;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("client")
@Data
public class ClientPropeties {
    private String id;
    private String password;
}
