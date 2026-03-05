package com.tomassirio.wanderer.command.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing the removal of a friendship between two users.
 *
 * <p>This is a persistence-only event used to delete bidirectional friendship entries from the
 * database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipRemovedEvent implements DomainEvent {
    private UUID userId;
    private UUID friendId;
}
