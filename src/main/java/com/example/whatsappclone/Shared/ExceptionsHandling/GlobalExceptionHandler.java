package com.example.whatsappclone.Shared.ExceptionsHandling;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public String globalHandler(Exception e){
        return e.getMessage();
    }
}
