package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.service.UserFollowService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID followUser(UUID followerId, UUID followedId) {
        log.info("User {} following user {}", followerId, followedId);

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new IllegalArgumentException("Already following this user");
        }

        // Pre-generate ID and timestamp
        UUID followId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                UserFollowedEvent.builder()
                        .followId(followId)
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(createdAt)
                        .build());

        log.info("User {} now follows user {}", followerId, followedId);

        return followId;
    }

    @Override
    public void unfollowUser(UUID followerId, UUID followedId) {
        log.info("User {} unfollowing user {}", followerId, followedId);

        // Publish event - persistence handler will delete from DB
        eventPublisher.publishEvent(
                UserUnfollowedEvent.builder()
                        .followerId(followerId)
                        .followedId(followedId)
                        .build());

        log.info("User {} unfollowed user {}", followerId, followedId);
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followedId) {
        return userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }
}
