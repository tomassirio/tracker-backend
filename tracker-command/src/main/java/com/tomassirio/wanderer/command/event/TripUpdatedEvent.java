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
public class TripUpdatedEvent {
    private UUID tripId;
    private Double latitude;
    private Double longitude;
    private Integer batteryLevel;
    private String message;
}
