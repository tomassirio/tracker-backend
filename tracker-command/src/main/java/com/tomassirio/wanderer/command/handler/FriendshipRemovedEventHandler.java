package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.commons.domain.Friendship;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipRemovedEventHandler implements EventHandler<FriendshipRemovedEvent> {

    private final EntityManager entityManager;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendshipRemovedEvent event) {
        log.debug(
                "Persisting FriendshipRemovedEvent between {} and {}",
                event.getUserId(),
                event.getFriendId());

        findFriendship(event.getUserId(), event.getFriendId()).ifPresent(entityManager::remove);

        findFriendship(event.getFriendId(), event.getUserId()).ifPresent(entityManager::remove);

        log.info("Friendship removed between {} and {}", event.getUserId(), event.getFriendId());
    }

    private Optional<Friendship> findFriendship(UUID userId, UUID friendId) {
        TypedQuery<Friendship> query =
                entityManager.createQuery(
                        "SELECT f FROM Friendship f WHERE f.userId = :userId AND f.friendId = :friendId",
                        Friendship.class);
        query.setParameter("userId", userId);
        query.setParameter("friendId", friendId);
        return query.getResultStream().findFirst();
    }
}
