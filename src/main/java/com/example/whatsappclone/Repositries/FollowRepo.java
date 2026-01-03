package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FollowRepo extends JpaRepository<Follow,String> {
   Optional<Follow> findByFollowerAndFollowing(User follower,User following);
   List<Follow> findByFollowingAndStatus(User following, Follow.Status status);

   Page<Follow> findByFollowerAndStatus(User follower,Follow.Status status,Pageable pageable);
    Page<Follow> findByFollowingAndStatus(User following,Follow.Status status,Pageable pageable);
    long countByFollowingAndStatus(User following,Follow.Status status);

    long countByFollowerAndStatus(User follower,Follow.Status status);

    boolean existsByFollowerAndFollowingAndStatus(User follower, User following, Follow.Status status);

    boolean existsByFollowerAndFollowing(User follower,User following);
Optional<Follow> findByUuidAndFollower(String uuid,User follower);
  Optional<Follow> findByUuidAndFollowing(String uuid,User following);
    List<Follow> findByFollower_IdAndFollowing_IdIn(
            String followerId,
            Collection<String> followingIds
    );
    List<Follow> findByFollowing_IdAndFollower_IdIn(
            String followingId,
            Collection<String> followerIds
    );

}
