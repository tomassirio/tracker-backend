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
public class TripPlanDeletedEvent implements DomainEvent {
    private UUID tripPlanId;
    private UUID userId;
}
