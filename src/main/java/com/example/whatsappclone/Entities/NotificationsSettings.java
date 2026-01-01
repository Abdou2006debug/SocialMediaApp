package com.example.whatsappclone.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NoArgsConstructor
@Data
public class NotificationsSettings{
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
   // @Column(name="likes")
    //private Boolean Onlikes;
    //@Column(name = "comments")
    //private Boolean Oncomments;
    //@Column(name="commentreplies")
    //private Boolean Oncommentreplies;
    @Column(name="follow")
    private Boolean Onfollow;
    @Column(name="followingrequests")
    private Boolean onfollowingrequestAccepted;
    @Column(name="followingrequests_rejected")
    private Boolean onfollowingrequestRejected;
    public NotificationsSettings(boolean onfollowingrequest_Accepted,boolean onfollowingrequest_rejected,boolean follow){
        this.onfollowingrequestAccepted =onfollowingrequest_Accepted;
        this.onfollowingrequestRejected =onfollowingrequest_rejected;
        this.Onfollow=follow;
    }
}