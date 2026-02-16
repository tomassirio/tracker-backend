package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.service.AchievementCalculationService;
import com.tomassirio.wanderer.commons.domain.Friendship;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
public class FriendshipCreatedEventHandler implements EventHandler<FriendshipCreatedEvent> {

    private final EntityManager entityManager;
    private final AchievementCalculationService achievementCalculationService;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendshipCreatedEvent event) {
        log.debug(
                "Persisting FriendshipCreatedEvent between {} and {}",
                event.getUserId(),
                event.getFriendId());

        Instant now = Instant.now();

        // Create bidirectional friendship
        if (!existsFriendship(event.getUserId(), event.getFriendId())) {
            Friendship friendship1 =
                    Friendship.builder()
                            .id(UUID.randomUUID())
                            .userId(event.getUserId())
                            .friendId(event.getFriendId())
                            .createdAt(now)
                            .build();
            entityManager.persist(friendship1);
        }

        if (!existsFriendship(event.getFriendId(), event.getUserId())) {
            Friendship friendship2 =
                    Friendship.builder()
                            .id(UUID.randomUUID())
                            .userId(event.getFriendId())
                            .friendId(event.getUserId())
                            .createdAt(now)
                            .build();
            entityManager.persist(friendship2);
        }

        log.info(
                "Friendship created and persisted between {} and {}",
                event.getUserId(),
                event.getFriendId());

        // Check friend achievements for both users
        achievementCalculationService.checkAndUnlockSocialAchievements(event.getUserId());
        achievementCalculationService.checkAndUnlockSocialAchievements(event.getFriendId());
    }

    private boolean existsFriendship(UUID userId, UUID friendId) {
        Long count =
                entityManager
                        .createQuery(
                                "SELECT COUNT(f) FROM Friendship f WHERE f.userId = :userId AND f.friendId = :friendId",
                                Long.class)
                        .setParameter("userId", userId)
                        .setParameter("friendId", friendId)
                        .getSingleResult();
        return count > 0;
    }
}
