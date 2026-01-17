package com.example.whatsappclone.Profile.persistence;

import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import org.springframework.data.repository.CrudRepository;

public interface ProfileInfoCacheRepo extends CrudRepository<ProfileInfo,String> {
    ProfileInfo findByUserId(String userId);
    boolean existsByUserId(String userId);
}
