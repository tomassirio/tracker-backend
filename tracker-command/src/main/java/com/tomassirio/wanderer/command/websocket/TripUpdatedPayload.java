package com.tomassirio.wanderer.command.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripUpdatedPayload {
    private UUID tripId;
    private Double latitude;
    private Double longitude;
    private Integer batteryLevel;
    private String message;
    private String city;
    private String country;
}
