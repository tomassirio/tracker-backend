package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting user unfollow events to the database.
 *
 * <p>This handler implements the CQRS write side by handling UserUnfollowedEvent and removing the
 * follow relationship from the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class UserUnfollowedEventHandler implements EventHandler<UserUnfollowedEvent> {

    private final UserFollowRepository userFollowRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
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
}
