package com.example.whatsappclone.User.persistence;

import com.example.whatsappclone.User.domain.cache.User;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepo extends CrudRepository<User,String> {
}
