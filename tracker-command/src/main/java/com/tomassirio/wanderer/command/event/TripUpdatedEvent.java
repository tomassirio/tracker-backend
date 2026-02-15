package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripUpdatedEvent implements DomainEvent {
    private UUID tripUpdateId;
    private UUID tripId;
    private GeoLocation location;
    private Integer batteryLevel;
    private String message;
    private Instant timestamp;
}
