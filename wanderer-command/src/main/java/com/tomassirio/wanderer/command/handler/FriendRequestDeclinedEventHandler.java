package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendRequestDeclinedEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting friend request decline events to the database.
 *
 * <p>This handler implements the CQRS write side by handling FriendRequestDeclinedEvent and
 * updating the friend request status in the database. Validation is performed in the service layer
 * before the event is emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestDeclinedEventHandler implements EventHandler<FriendRequestDeclinedEvent> {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendRequestDeclinedEvent event) {
        log.debug("Persisting FriendRequestDeclinedEvent for request: {}", event.getRequestId());

        friendRequestRepository
                .findById(event.getRequestId())
                .ifPresent(
                        request -> {
                            request.setStatus(FriendRequestStatus.DECLINED);
                            request.setUpdatedAt(Instant.now());

                            // No need to call save() - entity is managed and will be flushed
                            // automatically
                            log.info(
                                    "Friend request declined and persisted: {}",
                                    event.getRequestId());
                        });
    }
}
