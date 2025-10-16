package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.service.UserFollowService;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;

    @Override
    @Transactional
    public UserFollowResponse followUser(UUID followerId, UUID followedId) {
        log.info("User {} following user {}", followerId, followedId);

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new IllegalArgumentException("Already following this user");
        }

        UserFollow follow =
                UserFollow.builder()
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(Instant.now())
                        .build();

        UserFollow saved = userFollowRepository.save(follow);
        log.info("User {} now follows user {}", followerId, followedId);

        return new UserFollowResponse(
                saved.getId(), saved.getFollowerId(), saved.getFollowedId(), saved.getCreatedAt());
    }

    @Override
    @Transactional
    public void unfollowUser(UUID followerId, UUID followedId) {
        log.info("User {} unfollowing user {}", followerId, followedId);
        userFollowRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
        log.info("User {} unfollowed user {}", followerId, followedId);
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followedId) {
        return userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }
}
