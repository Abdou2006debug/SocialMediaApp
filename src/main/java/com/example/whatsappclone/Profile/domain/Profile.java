package com.example.whatsappclone.Profile.domain;


import com.example.whatsappclone.Identity.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(indexes={
        @Index(name="user_profile",columnList = "user_id")
        })
public class Profile {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String bio;
    private String username;
    private String privateavatarurl;
    private String publicavatarurl;
    private boolean showifonline=false;
    private boolean isprivate=false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(name ="user_id",insertable = false,updatable = false)
    private String userId;
public Profile(String bio , String username){
    this.bio=bio;
    this.username=username;
}
public Profile(boolean isprivate){
    this.isprivate=isprivate;
}
}
