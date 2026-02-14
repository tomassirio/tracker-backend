package com.tomassirio.wanderer.command.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripVisibilityChangedEvent implements DomainEvent {
    private UUID tripId;
    private String newVisibility;
    private String previousVisibility;
}
