package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
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
public class UserUnfollowedEventPersistenceHandler implements EventHandler<UserUnfollowedEvent> {

    private final UserFollowRepository userFollowRepository;

    @Override
    @EventListener
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
}
