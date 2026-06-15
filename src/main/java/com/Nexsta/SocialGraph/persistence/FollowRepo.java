package com.Nexsta.SocialGraph.persistence;

import com.Nexsta.SocialGraph.domain.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepo extends JpaRepository<Follow,UUID> {
   Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);
    long countByFollowingIdAndStatus(String followingId,Follow.Status status);
    long countByFollowerIdAndStatus(String followerId,Follow.Status status);
    boolean existsByFollowerIdAndFollowingIdAndStatus(String follower, String following, Follow.Status status);

    boolean existsByFollowerIdAndFollowingId(String followerId,String followingId);
    List<Follow> findByFollowerIdAndFollowingIdIn(
            String followerUuid,
            Collection<String> followings
    );
    List<Follow> findByFollowingIdAndFollowerIdIn(
            String followingUuid,
            Collection<String> followers
    );
    void deleteByFollowingIdAndStatus(String followingId, Follow.Status status);

    @Query(value="SELECT f.follower_id AS UserId FROM Follow f where f.following_id=:userId AND status:status AND f.follow_date<(SELECT f1.follow_date FROM Follow f1 WHERE f1.follower_id=: AND f1.following_id=:)",nativeQuery=true)
    List<UserId> find();

    interface UserId{
        String getUserId();
    }
}
