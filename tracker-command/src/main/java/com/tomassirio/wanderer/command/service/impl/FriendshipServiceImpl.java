package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.domain.Friendship;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;

    @Override
    public boolean areFriends(UUID userId, UUID friendId) {
        return friendshipRepository.existsByUserIdAndFriendId(userId, friendId)
                || friendshipRepository.existsByUserIdAndFriendId(friendId, userId);
    }

    @Override
    @Transactional
    public void createFriendship(UUID userId, UUID friendId) {
        log.info("Creating friendship between {} and {}", userId, friendId);

        Instant now = Instant.now();

        // Create bidirectional friendship
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            Friendship friendship1 =
                    Friendship.builder()
                            .userId(userId)
                            .friendId(friendId)
                            .createdAt(now)
                            .build();
            friendshipRepository.save(friendship1);
        }

        if (!friendshipRepository.existsByUserIdAndFriendId(friendId, userId)) {
            Friendship friendship2 =
                    Friendship.builder()
                            .userId(friendId)
                            .friendId(userId)
                            .createdAt(now)
                            .build();
            friendshipRepository.save(friendship2);
        }

        log.info("Friendship created between {} and {}", userId, friendId);
    }

    @Override
    @Transactional
    public void removeFriendship(UUID userId, UUID friendId) {
        log.info("Removing friendship between {} and {}", userId, friendId);

        friendshipRepository
                .findByUserIdAndFriendId(userId, friendId)
                .ifPresent(friendshipRepository::delete);

        friendshipRepository
                .findByUserIdAndFriendId(friendId, userId)
                .ifPresent(friendshipRepository::delete);

        log.info("Friendship removed between {} and {}", userId, friendId);
    }
}
