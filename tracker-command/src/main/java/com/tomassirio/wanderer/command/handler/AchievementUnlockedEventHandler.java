package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting achievement unlock events to the database.
 *
 * <p>This handler implements the CQRS write side by handling AchievementUnlockedEvent and
 * persisting user achievements to the database. WebSocket broadcasting is handled centrally by
 * {@link com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementUnlockedEventHandler implements EventHandler<AchievementUnlockedEvent> {

    private final EntityManager entityManager;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(AchievementUnlockedEvent event) {
        log.debug(
                "Persisting AchievementUnlockedEvent for user: {} achievement: {}",
                event.getUserId(),
                event.getAchievementType());

        // Use entityManager.getReference() to create proxies without loading the entities
        User user = entityManager.getReference(User.class, event.getUserId());
        Achievement achievement =
                entityManager.getReference(Achievement.class, event.getAchievementId());
        Trip trip =
                event.getTripId() != null
                        ? entityManager.getReference(Trip.class, event.getTripId())
                        : null;

        UserAchievement unlockedAchievement =
                UserAchievement.builder()
                        .id(event.getUserAchievementId())
                        .user(user)
                        .achievement(achievement)
                        .trip(trip)
                        .unlockedAt(event.getUnlockedAt())
                        .valueAchieved(event.getValueAchieved())
                        .build();

        entityManager.persist(unlockedAchievement);
        log.info(
                "Achievement unlocked and persisted: {} for user: {}",
                event.getAchievementType(),
                event.getUserId());
    }
}
