package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting user follow events to the database.
 *
 * <p>This handler implements the CQRS write side by handling UserFollowedEvent and persisting the
 * follow relationship to the database, then broadcasting via WebSocket after commit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserFollowedEventPersistenceHandler implements EventHandler<UserFollowedEvent> {

    private final UserFollowRepository userFollowRepository;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(UserFollowedEvent event) {
        log.debug(
                "Persisting UserFollowedEvent: {} follows {}",
                event.getFollowerId(),
                event.getFollowedId());

        UserFollow follow =
                UserFollow.builder()
                        .id(event.getFollowId())
                        .followerId(event.getFollowerId())
                        .followedId(event.getFollowedId())
                        .createdAt(event.getCreatedAt())
                        .build();

        userFollowRepository.save(follow);
        log.info(
                "User follow created and persisted: {} follows {}",
                event.getFollowerId(),
                event.getFollowedId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(UserFollowedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug(
                    "Broadcasting UserFollowedEvent: {} follows {}",
                    event.getFollowerId(),
                    event.getFollowedId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
