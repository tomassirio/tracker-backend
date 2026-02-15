package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.commons.domain.Friendship;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class FriendshipCreatedEventHandler implements EventHandler<FriendshipCreatedEvent> {

    private final FriendshipRepository friendshipRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FriendshipCreatedEvent event) {
        log.debug(
                "Persisting FriendshipCreatedEvent between {} and {}",
                event.getUserId(),
                event.getFriendId());

        Instant now = Instant.now();

        // Create bidirectional friendship
        if (!friendshipRepository.existsByUserIdAndFriendId(
                event.getUserId(), event.getFriendId())) {
            Friendship friendship1 =
                    Friendship.builder()
                            .userId(event.getUserId())
                            .friendId(event.getFriendId())
                            .createdAt(now)
                            .build();
            friendshipRepository.save(friendship1);
        }

        if (!friendshipRepository.existsByUserIdAndFriendId(
                event.getFriendId(), event.getUserId())) {
            Friendship friendship2 =
                    Friendship.builder()
                            .userId(event.getFriendId())
                            .friendId(event.getUserId())
                            .createdAt(now)
                            .build();
            friendshipRepository.save(friendship2);
        }

        log.info(
                "Friendship created and persisted between {} and {}",
                event.getUserId(),
                event.getFriendId());
    }
}
