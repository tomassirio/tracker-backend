package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.UserFollow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    Optional<UserFollow> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    List<UserFollow> findByFollowedId(UUID followedId);

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    void deleteByFollowerIdOrFollowedId(UUID followerId, UUID followedId);
}
