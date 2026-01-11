package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redis.Repositries.UserCacheRepo;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileCacheRepo;
import com.example.whatsappclone.Profile.persistence.ProfileInfoCacheRepo;
import com.example.whatsappclone.Shared.Mappers.Cachemapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CacheQueryService {

  private final ProfileCacheRepo profileCacheRepo;
  private final ProfileInfoCacheRepo profileInfoCacheRepo;
  private final Cachemapper mapper;
  private final RedisTemplate<String,String> redisTemplate;


  public Optional<User>  getUser(String userId){
    return userCacheRepo.findById(userId).map(mapper::getUser);
  }






}
