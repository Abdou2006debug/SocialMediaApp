package com.example.whatsappclone.SocialGraph.domain.events;

import com.example.whatsappclone.SocialGraph.domain.Follow;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class followAdded {
    private Follow follow;
}
