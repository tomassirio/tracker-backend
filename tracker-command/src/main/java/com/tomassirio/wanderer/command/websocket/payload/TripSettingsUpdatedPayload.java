package com.tomassirio.wanderer.command.websocket.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSettingsUpdatedPayload {
    private UUID tripId;
    private Integer updateRefresh;
    private Boolean automaticUpdates;
}
