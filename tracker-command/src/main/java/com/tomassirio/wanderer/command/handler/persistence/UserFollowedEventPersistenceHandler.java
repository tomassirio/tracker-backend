package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.commons.domain.UserFollow;
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
public class UserFollowedEventPersistenceHandler implements EventHandler<UserFollowedEvent> {

    private final UserFollowRepository userFollowRepository;

    @Override
    @EventListener
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
}
