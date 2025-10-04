package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.command.validation.PastOrPresentInstantString;
import jakarta.validation.constraints.*;

/**
 * Request DTO for location updates.
 * Contains GPS coordinates and metadata from tracking devices like OwnTracks.
 */
public record LocationUpdateRequest(
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
        Double longitude,

        @PastOrPresentInstantString
        String timestamp, // ISO-8601 format

        @DecimalMin(value = "-1000.0", message = "Altitude seems too low")
        @DecimalMax(value = "10000.0", message = "Altitude seems too high")
        Double altitude,

        @DecimalMin(value = "0.0", message = "Accuracy must be positive")
        Double accuracy,

        @Min(value = 0, message = "Battery level must be between 0 and 100")
        @Max(value = 100, message = "Battery level must be between 0 and 100")
        Integer batteryLevel,

        @Size(max = 100, message = "Source must be less than 100 characters")
        String source
) {
}
