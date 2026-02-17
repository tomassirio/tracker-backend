package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
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
 * Event handler for persisting friend request acceptance events to the database.
 *
 * <p>This handler implements the CQRS write side by handling FriendRequestAcceptedEvent and
 * updating the friend request status in the database. Validation is performed in the service layer
 * before the event is emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestAcceptedEventHandler implements EventHandler<FriendRequestAcceptedEvent> {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendRequestAcceptedEvent event) {
        log.debug("Persisting FriendRequestAcceptedEvent for request: {}", event.getRequestId());

        friendRequestRepository
                .findById(event.getRequestId())
                .ifPresent(
                        request -> {
                            request.setStatus(FriendRequestStatus.ACCEPTED);
                            request.setUpdatedAt(Instant.now());

                            // No need to call save() - entity is managed and will be flushed
                            // automatically
                            log.info(
                                    "Friend request accepted and persisted: {}",
                                    event.getRequestId());
                        });
    }
}
