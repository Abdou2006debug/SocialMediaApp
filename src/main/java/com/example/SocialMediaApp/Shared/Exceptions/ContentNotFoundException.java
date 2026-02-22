package com.example.SocialMediaApp.Shared.Exceptions;

public class ContentNotFoundException extends RuntimeException{
    public ContentNotFoundException(String message){
        super(message);
    }
}
