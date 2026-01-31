package com.example.whatsappclone.Shared;

import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserExistence {

    private final UserRepo userRepo;


    @Before("@annotation(CheckUserExistence)")
    public void checkUserExistence(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();

            if (args[0] instanceof String userId) {
                log.info("checking user existence for "+userId);
                if (!userRepo.existsById(userId)) {
                    throw new UserNotFoundException("User not found");
                }
            }
    }
}
