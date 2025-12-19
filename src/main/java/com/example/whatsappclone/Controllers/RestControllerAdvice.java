package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.Map;

@org.springframework.web.bind.annotation.RestControllerAdvice
public class RestControllerAdvice {
    @ExceptionHandler(BadFollowRequestException.class)
    public ResponseEntity<Map<String,String>> handleBadFollowRequest(BadFollowRequestException ex){
        Map<String,String> map=Map.of("message",ex.getMessage());
        HttpStatus status=ex.getMessage().equals("follow not related to current user")? HttpStatus.NOT_FOUND: HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(map,status);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> ha(){
        return new ResponseEntity<>("error",HttpStatus.BAD_REQUEST);
    }
}
