package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting friend request acceptance events to the database.
 *
 * <p>This handler implements the CQRS write side by handling FriendRequestAcceptedEvent and
 * updating the friend request status in the database. Validation is performed in the service layer
 * before the event is emitted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestAcceptedEventPersistenceHandler
        implements EventHandler<FriendRequestAcceptedEvent> {

    private final FriendRequestRepository friendRequestRepository;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FriendRequestAcceptedEvent event) {
        log.debug("Persisting FriendRequestAcceptedEvent for request: {}", event.getRequestId());

        // Friend request is validated in the service layer before event emission
        FriendRequest request = friendRequestRepository.getReferenceById(event.getRequestId());

        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setUpdatedAt(Instant.now());

        friendRequestRepository.save(request);
        log.info("Friend request accepted and persisted: {}", event.getRequestId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(FriendRequestAcceptedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting FriendRequestAcceptedEvent: {}", event.getRequestId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
