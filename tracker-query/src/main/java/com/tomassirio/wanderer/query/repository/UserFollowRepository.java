package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.UserFollow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    List<UserFollow> findByFollowerId(UUID followerId);

    List<UserFollow> findByFollowedId(UUID followedId);

    long countByFollowedId(UUID followedId);
}
