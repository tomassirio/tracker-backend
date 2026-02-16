package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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
public class UserUnfollowedEventHandler implements EventHandler<UserUnfollowedEvent> {

    private final EntityManager entityManager;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(UserUnfollowedEvent event) {
        log.debug(
                "Persisting UserUnfollowedEvent: {} unfollows {}",
                event.getFollowerId(),
                event.getFollowedId());

        TypedQuery<UserFollow> query =
                entityManager.createQuery(
                        "SELECT uf FROM UserFollow uf WHERE uf.followerId = :followerId AND uf.followedId = :followedId",
                        UserFollow.class);
        query.setParameter("followerId", event.getFollowerId());
        query.setParameter("followedId", event.getFollowedId());
        query.getResultStream().findFirst().ifPresent(entityManager::remove);

        log.info(
                "User unfollow persisted: {} unfollowed {}",
                event.getFollowerId(),
                event.getFollowedId());
    }
}
