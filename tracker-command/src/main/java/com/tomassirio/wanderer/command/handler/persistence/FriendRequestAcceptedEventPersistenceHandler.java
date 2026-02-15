package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
public class FriendRequestAcceptedEventPersistenceHandler
        implements EventHandler<FriendRequestAcceptedEvent> {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FriendRequestAcceptedEvent event) {
        log.debug("Persisting FriendRequestAcceptedEvent for request: {}", event.getRequestId());

        FriendRequest request =
                friendRequestRepository
                        .findById(event.getRequestId())
                        .orElseThrow(() -> new EntityNotFoundException("Friend request not found"));

        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setUpdatedAt(Instant.now());

        friendRequestRepository.save(request);
        log.info("Friend request accepted and persisted: {}", event.getRequestId());
    }
}
