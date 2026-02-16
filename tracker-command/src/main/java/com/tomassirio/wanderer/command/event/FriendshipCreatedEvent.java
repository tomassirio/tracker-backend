package com.tomassirio.wanderer.command.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing the creation of a friendship between two users.
 *
 * <p>This is a persistence-only event used to create bidirectional friendship entries in the
 * database. WebSocket notifications for friend request acceptance are handled by {@link
 * FriendRequestAcceptedEvent}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipCreatedEvent implements DomainEvent {
    private UUID friendshipId;
    private UUID userId;
    private UUID friendId;
}
