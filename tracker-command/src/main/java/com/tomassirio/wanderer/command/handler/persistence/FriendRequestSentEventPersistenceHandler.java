package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
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
public class FriendRequestSentEventPersistenceHandler
        implements EventHandler<FriendRequestSentEvent> {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FriendRequestSentEvent event) {
        log.debug("Persisting FriendRequestSentEvent for request: {}", event.getRequestId());

        FriendRequest request =
                FriendRequest.builder()
                        .id(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status(FriendRequestStatus.valueOf(event.getStatus()))
                        .createdAt(event.getCreatedAt())
                        .build();

        friendRequestRepository.save(request);
        log.info("Friend request created and persisted: {}", event.getRequestId());
    }
}
