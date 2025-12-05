package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FollowRepo extends JpaRepository<Follow,String> {
    Optional<Follow> findByFollowerAndFollowingAndStatus(User follower, User following, Follow.Status status);
   Optional<Follow> findByFollowerAndFollowing(User follower,User following);
   List<Follow> findByFollowingAndStatus(User following, Follow.Status status);

  org.springframework.data.domain.Page<Follow> findByFollowingAndStatus(User following, Follow.Status status, org.springframework.data.domain.Pageable pageable);
   Page<Follow> findByFollowerAndStatus(User follower, Follow.Status status, Pageable pageable);

    long countByFollowingAndStatus(User user, Follow.Status status);

    long countByFollowerAndStatus(User user, Follow.Status status);

    boolean existsByFollowerAndFollowingAndStatus(User follower, User following, Follow.Status status);

    boolean existsByFollowerAndFollowing_UuidAndStatus(User follower, String followingUuid, Follow.Status status);
    boolean existsByFollowerAndFollowing(User follower,User following);
Optional<Follow> findByUuidAndFollower(String uuid,User follower);
  Optional<Follow> findByUuidAndFollowing(String uuid,User following);

    boolean existsByFollowerAndFollowingUuid(User follower, String followinguuid);

    boolean existsByFollower_UuidAndFollowingAndStatus(String followeruuid, User following, Follow.Status status);
}
