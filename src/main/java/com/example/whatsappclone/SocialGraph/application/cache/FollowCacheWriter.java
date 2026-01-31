package com.example.whatsappclone.SocialGraph.application.cache;

import com.example.whatsappclone.Shared.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.SocialGraph.application.FollowQueryHelper;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowCacheWriter {
      private final RedisTemplate<String,String> redisTemplate;
      private final FollowRepo followRepo;


    public Optional<Set<String>>  getUserCachedFollows(String userId, int page, FollowQueryHelper.Position position){
        int start=page*20;
        int end=(page+1)*20-1;

        StringBuilder key=new StringBuilder("user:"+userId);
        StringBuilder key1= FollowQueryHelper.Position.FOLLOWERS==position?
                key.append("followers"):key.append("followings");

          //  log.info("getting followings for "+userId+" from cache");
            Set<String> followsIds = redisTemplate.opsForZSet().reverseRange(key1.toString(),start,end);
            if(followsIds!=null&&!followsIds.isEmpty()){
                return Optional.of(followsIds);
            }
            return Optional.empty();
        }

      public Set<String> cacheWindow(String userId, int page, FollowQueryHelper.Position position){
        // old pages no cache hit db directly
          if(4<page){
              return position== FollowQueryHelper.Position.FOLLOWERS
                      ?cacheUserFollowers(userId,page):cacheUserFollowings(userId,page);
          }
          Optional<Set<String>> followIds= getUserCachedFollows(userId,page,position);
          followIds.orElseGet(()->position== FollowQueryHelper.Position.FOLLOWERS?
                  cacheUserFollowers(userId,page):cacheUserFollowings(userId,page));
          return followIds.orElseThrow(()->new FollowListNotVisibleException("sorry we can't fetch user list of "+position.name()+" try later"));
      }

      public Set<String> cacheUserFollowers(String userId, int page) {
        Pageable pageable= PageRequest.of(page,20, Sort.by("accepteddate").descending());
        Page<Follow> followerspage = followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        Set<String> followersIds=followers.stream().map(Follow::getFollower_id).collect(Collectors.toSet());
        // caching only  first 80 followers
        if(page<4){
            followers.forEach(follow -> redisTemplate.opsForZSet().
                    add("user:"+userId+":followers",follow.getFollower_id(),follow.getAccepteddate().getEpochSecond()));
            redisTemplate.expire("user:"+userId+":followers",10, TimeUnit.MINUTES);
        }
        return followersIds;
    }

    public Set<String> cacheUserFollowings(String userId, int page) {
        Pageable pageable= PageRequest.of(page,20, Sort.by("accepteddate").descending());
        Page<Follow> followingspage = followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followings= followingspage.getContent();
        Set<String> followingsIds=followings.stream().map(Follow::getFollowing_id).collect(Collectors.toSet());
        // caching only  first 80 followings
        if(page<4){
            followings.forEach(follow -> redisTemplate.opsForZSet().
                    add("user:"+userId+":followings",follow.getFollowing_id(),follow.getAccepteddate().getEpochSecond()));
            redisTemplate.expire("user:"+userId+":followings",10, TimeUnit.MINUTES);
        }
        return followingsIds;
    }

    public String UserFollowerCount(String userId){
          String countCache=redisTemplate.opsForValue().get("user:followers:"+userId);
          if(countCache!=null){
              return countCache;
          }
          long count=followRepo.countByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED);
          redisTemplate.opsForValue().set("user:followers:"+userId,String.valueOf(count));
          redisTemplate.expire("user:followers:"+userId,20,TimeUnit.MINUTES);
          return String.valueOf(count);
    }

    public String UserFollowingCount(String userId){
        String countCache=redisTemplate.opsForValue().get("user:followings:"+userId);
        if(countCache!=null){
            return countCache;
        }
        long count=followRepo.countByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED);
        redisTemplate.opsForValue().set("user:followings:"+userId,String.valueOf(count));
        redisTemplate.expire("user:followings:"+userId,20,TimeUnit.MINUTES);
        return String.valueOf(count);
    }
}


