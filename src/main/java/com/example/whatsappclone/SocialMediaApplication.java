package com.example.whatsappclone;

import com.example.whatsappclone.Storage.StorageProperties;
import com.example.whatsappclone.User.application.ClientPropeties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({StorageProperties.class, ClientPropeties.class})
public class SocialMediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialMediaApplication.class, args);
    }
}
