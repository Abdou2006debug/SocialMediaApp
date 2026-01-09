package com.example.whatsappclone.Identity.persistence;

import com.example.whatsappclone.Identity.domain.cache.User;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepo extends CrudRepository<User,String> {
}
