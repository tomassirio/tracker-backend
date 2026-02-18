package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipRemovedEventHandler implements EventHandler<FriendshipRemovedEvent> {

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendshipRemovedEvent event) {
        log.debug(
                "Persisting FriendshipRemovedEvent between {} and {}",
                event.getUserId(),
                event.getFriendId());

        // Delete friendship entries (bidirectional)
        friendshipRepository
                .findByUserIdAndFriendId(event.getUserId(), event.getFriendId())
                .ifPresent(friendshipRepository::delete);

        friendshipRepository
                .findByUserIdAndFriendId(event.getFriendId(), event.getUserId())
                .ifPresent(friendshipRepository::delete);

        // Delete the accepted friend request so users can send new requests
        // Check both directions since either user could have been the sender
        friendRequestRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        event.getUserId(), event.getFriendId(), FriendRequestStatus.ACCEPTED)
                .ifPresent(friendRequestRepository::delete);

        friendRequestRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        event.getFriendId(), event.getUserId(), FriendRequestStatus.ACCEPTED)
                .ifPresent(friendRequestRepository::delete);

        log.info("Friendship removed between {} and {}", event.getUserId(), event.getFriendId());
    }
}
