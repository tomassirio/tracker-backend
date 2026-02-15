package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
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
public class FriendshipRemovedEventHandler implements EventHandler<FriendshipRemovedEvent> {

    private final FriendshipRepository friendshipRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FriendshipRemovedEvent event) {
        log.debug(
                "Persisting FriendshipRemovedEvent between {} and {}",
                event.getUserId(),
                event.getFriendId());

        friendshipRepository
                .findByUserIdAndFriendId(event.getUserId(), event.getFriendId())
                .ifPresent(friendshipRepository::delete);

        friendshipRepository
                .findByUserIdAndFriendId(event.getFriendId(), event.getUserId())
                .ifPresent(friendshipRepository::delete);

        log.info("Friendship removed between {} and {}", event.getUserId(), event.getFriendId());
    }
}
