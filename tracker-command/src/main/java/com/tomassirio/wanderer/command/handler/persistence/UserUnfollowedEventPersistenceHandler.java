package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting user unfollow events to the database.
 *
 * <p>This handler implements the CQRS write side by handling UserUnfollowedEvent and removing the
 * follow relationship from the database, then broadcasting via WebSocket after commit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserUnfollowedEventPersistenceHandler implements EventHandler<UserUnfollowedEvent> {

    private final UserFollowRepository userFollowRepository;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(UserUnfollowedEvent event) {
        log.debug(
                "Persisting UserUnfollowedEvent: {} unfollows {}",
                event.getFollowerId(),
                event.getFollowedId());

        userFollowRepository.deleteByFollowerIdAndFollowedId(
                event.getFollowerId(), event.getFollowedId());

        log.info(
                "User unfollow persisted: {} unfollowed {}",
                event.getFollowerId(),
                event.getFollowedId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(UserUnfollowedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug(
                    "Broadcasting UserUnfollowedEvent: {} unfollows {}",
                    event.getFollowerId(),
                    event.getFollowedId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
