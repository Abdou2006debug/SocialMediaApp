package com.example.SocialMediaApp.Content.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Like {

    @Id
    @GeneratedValue
    private UUID id;




}
