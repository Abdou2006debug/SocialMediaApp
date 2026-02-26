package com.example.SocialMediaApp.Content.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostSettings {

    private Boolean commentsDisabled=false;
    private Boolean hideLikes=false;

}
