package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileCacheRepo;
import com.example.whatsappclone.Profile.persistence.ProfileInfoCacheRepo;
import com.example.whatsappclone.Shared.Mappers.Cachemapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CacheWriterService {


    private final UserCacheRepo usercacheRepo;
    private final ProfileCacheRepo profileCacheRepo;
    private final ProfileInfoCacheRepo profileInfoCacheRepo;
    private final Cachemapper mapper;

    public void cacheUser(
            User user){
    com.example.whatsappclone.Configurations.Redis.RedisClasses.User cachedUser=mapper.cacheUser(user);
    usercacheRepo.save(cachedUser);
    }




}

