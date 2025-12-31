package com.example.whatsappclone.Events;

import com.example.whatsappclone.Entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class notification {
    private User trigger;
    private User recipient;
    private notificationType type;
    private String message;
    private String followid;
    public notification(User trigger, User recipient, notificationType type){
        this(trigger,recipient,type,null);
    }
    public notification(User trigger, User recipient, notificationType type, String followid){
        this.trigger=trigger;
        this.recipient=recipient;
        this.type=type;
        this.message=message;
        this.followid=followid;
    }
    public enum notificationType{FOLLOW,FOLLOW_REQUESTED,FOLLOWING_ACCEPTED,FOLLOWING_REJECTED}
}
