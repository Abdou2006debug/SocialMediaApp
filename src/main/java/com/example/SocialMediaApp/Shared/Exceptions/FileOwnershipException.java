package com.example.SocialMediaApp.Shared.Exceptions;

public class FileOwnershipException extends RuntimeException{
    public FileOwnershipException(String message){
        super(message);
    }
}
