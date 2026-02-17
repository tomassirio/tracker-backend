package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.command.repository.AchievementRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserAchievementRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
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

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final TripRepository tripRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(AchievementUnlockedEvent event) {
        log.debug(
                "Persisting AchievementUnlockedEvent for user: {} achievement: {}",
                event.getUserId(),
                event.getAchievementType());

        // Use repository.getReferenceById() to create proxies without loading the entities
        User user = userRepository.getReferenceById(event.getUserId());
        Achievement achievement = achievementRepository.getReferenceById(event.getAchievementId());
        Trip trip =
                event.getTripId() != null
                        ? tripRepository.getReferenceById(event.getTripId())
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

        userAchievementRepository.save(unlockedAchievement);
        log.info(
                "Achievement unlocked and persisted: {} for user: {}",
                event.getAchievementType(),
                event.getUserId());
    }
}
