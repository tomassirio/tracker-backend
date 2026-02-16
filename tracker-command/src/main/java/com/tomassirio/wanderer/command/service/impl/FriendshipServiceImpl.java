package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.service.FriendshipService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean areFriends(UUID userId, UUID friendId) {
        return friendshipRepository.existsByUserIdAndFriendId(userId, friendId)
                || friendshipRepository.existsByUserIdAndFriendId(friendId, userId);
    }

    @Override
    public void createFriendship(UUID userId, UUID friendId) {
        log.info("Creating friendship between {} and {}", userId, friendId);

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                FriendshipCreatedEvent.builder().userId(userId).friendId(friendId).build());

        log.info("Friendship created between {} and {}", userId, friendId);
    }

    @Override
    public void removeFriendship(UUID userId, UUID friendId) {
        log.info("Removing friendship between {} and {}", userId, friendId);

        // Publish event - persistence handler will delete from DB
        eventPublisher.publishEvent(
                FriendshipRemovedEvent.builder().userId(userId).friendId(friendId).build());

        log.info("Friendship removed between {} and {}", userId, friendId);
    }
}
