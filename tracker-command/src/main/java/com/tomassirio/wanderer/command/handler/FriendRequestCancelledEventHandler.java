package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendRequestCancelledEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting friend request cancellation events to the database.
 *
 * <p>This handler implements the CQRS write side by handling FriendRequestCancelledEvent and
 * deleting the friend request from the database. Validation is performed in the service layer
 * before the event is emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestCancelledEventHandler
        implements EventHandler<FriendRequestCancelledEvent> {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendRequestCancelledEvent event) {
        log.debug("Persisting FriendRequestCancelledEvent for request: {}", event.getRequestId());

        friendRequestRepository
                .findById(event.getRequestId())
                .ifPresent(
                        request -> {
                            friendRequestRepository.delete(request);
                            log.info(
                                    "Friend request cancelled and deleted: {}",
                                    event.getRequestId());
                        });
    }
}

