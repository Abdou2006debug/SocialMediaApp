package com.example.whatsappclone.User.domain.cache;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.time.LocalDate;

@RedisHash(timeToLive = 240,value = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String uuid;

    private Instant createddate;
    private Instant lastmodifieddate;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private LocalDate birthday;
}
