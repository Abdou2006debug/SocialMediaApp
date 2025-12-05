package com.example.whatsappclone.Websocketconfig;


import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class Authchannelinterceptor implements ChannelInterceptor {

private final JwtDecoder jwtDecoder;
private final Logger log= LoggerFactory.getLogger(Authchannelinterceptor.class);
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if(accessor==null){
            throw new BadRequestException();
        }
        boolean isconnect=accessor.getCommand().equals(StompCommand.CONNECT);
        if(isconnect){
            try{
                String header=accessor.getFirstNativeHeader("Authorization");
                String token=header.substring(7);
                Jwt jwt= jwtDecoder.decode(token);
               String username= jwt.getClaims().get("preferred_username").toString();
                accessor.setUser(()->username);
                log.info(username);
            }catch (Exception e){
                throw new BadRequestException("something went wrong");
            }
        }
        return message;
    }
}

