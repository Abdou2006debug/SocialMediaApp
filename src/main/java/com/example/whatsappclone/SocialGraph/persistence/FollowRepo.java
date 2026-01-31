package com.example.whatsappclone.SocialGraph.persistence;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FollowRepo extends JpaRepository<Follow,String> {
   Optional<Follow> findByFollowerAndFollowing(User follower, User following);
   List<Follow> findByFollowingAndStatus(User following, Follow.Status status);
   Page<Follow> findByFollowerAndStatus(User follower,Follow.Status status,Pageable pageable);
    Page<Follow> findByFollowingAndStatus(User following,Follow.Status status,Pageable pageable);
    long countByFollowingAndStatus(User following,Follow.Status status);

    long countByFollowerAndStatus(User follower,Follow.Status status);

    boolean existsByFollowerAndFollowingAndStatus(User follower, User following, Follow.Status status);

    boolean existsByFollowerAndFollowing(User follower,User following);
Optional<Follow> findByUuidAndFollower(String uuid,User follower);
  Optional<Follow> findByUuidAndFollowing(String uuid,User following);

    List<Follow> findByFollower_UuidAndFollowing_UuidIn(
            String followerUuid,
            Collection<String> followings
    );
    List<Follow> findByFollowing_UuidAndFollower_UuidIn(
            String followingUuid,
            Collection<String> followers
    );
    void deleteByFollowingAndStatus(User following, Follow.Status status);
}
