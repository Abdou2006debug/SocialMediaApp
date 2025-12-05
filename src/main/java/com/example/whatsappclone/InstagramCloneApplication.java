package com.example.whatsappclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InstagramCloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstagramCloneApplication.class, args);
    }
}
