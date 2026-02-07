package com.example.SocialMediaApp.SocialGraph.application.cache;

import com.example.SocialMediaApp.Shared.Exceptions.PageNotExistException;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
      private final ThreadPoolTaskExecutor TaskExecutor;


    public Optional<Set<String>>  getUserCachedFollows(String userId, int page, FollowQueryHelper.Position position){
        User targetUser=new User(userId);
        Pageable pageable=PageRequest.of(page,1);
        Page<Follow> Page=position== FollowQueryHelper.Position.FOLLOWERS?
                followRepo.findByFollowingAndStatus(targetUser, Follow.Status.ACCEPTED,pageable):followRepo.findByFollowerAndStatus(targetUser, Follow.Status.ACCEPTED,pageable);
        if(!Page.getContent().isEmpty()){
            Follow follow=Page.getContent().get(0);
            String key=position== FollowQueryHelper.Position.FOLLOWERS?":followers:":":followings:";
            Set<String> Ids = redisTemplate.opsForZSet()
                    .reverseRangeByScore(
                            "user:" + userId + key,
                            follow.getFollowDate().getEpochSecond(),
                            0,
                            0,
                            20
                    );
            if(Ids.isEmpty()){
                return Optional.empty();
            }
            return Optional.of(Ids);
        }
        throw new PageNotExistException();
        }

      public Set<String> cacheWindow(String userId, int page, FollowQueryHelper.Position position){
         if(page<4){
             try{
                 Optional<Set<String>> Ids= getUserCachedFollows(userId,page,position);
                 if(Ids.isPresent()){
                     log.info("getting "+userId+" "+position.name()+" from cache");
                     return Ids.get();
             }
                 // if page does not exists no need to go further
             }catch (PageNotExistException e){
                 return new HashSet<>();
             }
         }
          return position== FollowQueryHelper.Position.FOLLOWERS
                  ?cacheUserFollowers(userId,page):cacheUserFollowings(userId,page);
      }

      public Set<String> cacheUserFollowers(String userId, int page) {
        Pageable pageable= PageRequest.of(page,20, Sort.by("followDate").descending());
        Page<Follow> followerspage = followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        Set<String> followersIds=followers.stream().map(Follow::getFollower_id).collect(Collectors.toSet());
        // caching only the first 3 pages of followers
        if(page<4){
            log.info("caching "+userId+" followers");
            // doing the cache in a worker thread to not block the request thread
            TaskExecutor.execute(()->{
                try{
                    followers.forEach(follow -> redisTemplate.opsForZSet().
                            add("user:"+userId+":followers:",follow.getFollower_id(),follow.getFollowDate().getEpochSecond()));
                    redisTemplate.expire("user:"+userId+":followers:",10, TimeUnit.MINUTES);
                } catch(Exception e){
                log.error("failed to cache "+userId+" followers "+e.getMessage());
            }
                if(page==0){
                    redisTemplate.opsForValue().set("user:" + userId + ":followers:page0_cached", "1", 10, TimeUnit.MINUTES);
                }
        });

        }
          return followersIds;
      }

    public Set<String> cacheUserFollowings(String userId, int page) {
        Pageable pageable= PageRequest.of(page,20, Sort.by("followDate").descending());
        Page<Follow> followingspage = followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followings= followingspage.getContent();
        Set<String> followingsIds=followings.stream().map(Follow::getFollowing_id).collect(Collectors.toSet());
        // caching only the first 3 pages of followings
        if(page<4){
            log.info("caching "+userId+" followings");
            TaskExecutor.execute(()->{
                try{
                    followings.forEach(follow -> redisTemplate.opsForZSet().
                            add("user:"+userId+":followings:",follow.getFollowing_id(),follow.getFollowDate().getEpochSecond()));
                    redisTemplate.expire("user:"+userId+":followings:",10, TimeUnit.MINUTES);
                }catch (Exception e){
                    log.error("failed to cache "+userId+" followings "+e.getMessage());
                }
                if(page==0){
                    redisTemplate.opsForValue().set("user:" + userId + ":followings:page0_cached", "1", 10, TimeUnit.MINUTES);
                }
            });
        }
        return followingsIds;
    }

    public String UserFollowerCount(String userId){
          String countCache=redisTemplate.opsForValue().get("user:followers:"+userId);
          if(countCache!=null){
              log.info("getting the followers count from the cache");
              return countCache;
          }
          log.info("getting the followers count from db and caching");
          long count=followRepo.countByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED);
          redisTemplate.opsForValue().set("user:followers:"+userId,String.valueOf(count));
          redisTemplate.expire("user:followers:"+userId,20,TimeUnit.MINUTES);
          return String.valueOf(count);
    }

    public String UserFollowingCount(String userId){
        String countCache=redisTemplate.opsForValue().get("user:followings:"+userId);
        if(countCache!=null){
            log.info("getting the followings count from the cache");
            return countCache;
        }
        log.info("getting the followings count from db and caching");
        long count=followRepo.countByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED);
        redisTemplate.opsForValue().set("user:followings:"+userId,String.valueOf(count));
        redisTemplate.expire("user:followings:"+userId,20,TimeUnit.MINUTES);
        return String.valueOf(count);
    }

}


