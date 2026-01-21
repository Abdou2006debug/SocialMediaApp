package com.example.whatsappclone.User.application;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticatedUserService {

   // private final CacheQueryService cacheQueryService;
    private final UserRepo userRepo;
   // private final CacheWriterService cacheWriterService;

    public User getcurrentuser(boolean fetchfullinfo){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        String userId=((Jwt) authentication.getPrincipal()).getSubject();
        if(userId==null){
            throw new AuthenticationCredentialsNotFoundException("something went wrong trying to authenticate you please try later");
        }
        if(fetchfullinfo){
           // Optional<User> cacheduser=cacheQueryService.getUser(userId);
          //  if(cacheduser.isPresent()){
          //      return cacheduser.get();
          //  }
            User user=  userRepo.findById(userId).
                    orElseThrow(()->new UserNotFoundException("user not found"));
            //cacheWriterService.cacheUser(user);
            return user;
        }
        return new User(userId);
    }
}
