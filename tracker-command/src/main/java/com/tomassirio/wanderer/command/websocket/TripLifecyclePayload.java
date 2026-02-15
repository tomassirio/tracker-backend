package com.tomassirio.wanderer.command.websocket;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripLifecyclePayload {
    private UUID tripId;
    private String tripName;
    private UUID ownerId;
    private String visibility;
}
