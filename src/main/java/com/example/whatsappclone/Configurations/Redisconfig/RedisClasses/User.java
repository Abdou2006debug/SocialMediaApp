package com.example.whatsappclone.Configurations.Redisconfig.RedisClasses;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

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
