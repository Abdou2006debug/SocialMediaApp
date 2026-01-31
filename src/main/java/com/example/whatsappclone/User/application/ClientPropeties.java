package com.example.whatsappclone.User.application;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("client")
@Data
public class ClientPropeties {
    private String id;
    private String password;
}
