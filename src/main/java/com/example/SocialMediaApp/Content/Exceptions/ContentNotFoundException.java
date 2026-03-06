package com.example.SocialMediaApp.Content.Exceptions;

public class ContentNotFoundException extends RuntimeException{
    public ContentNotFoundException(String message){
        super(message);
    }
}
