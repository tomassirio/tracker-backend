package com.tomassirio.wanderer.command.service.helper;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import org.springframework.stereotype.Component;

/**
 * Helper component responsible for ensuring Trip embedded objects are properly initialized. This
 * handles cases where embedded objects might be null when loaded from the database.
 */
@Component
public class TripEmbeddedObjectsInitializer {

    /**
     * Creates a new TripSettings with the specified visibility. Used during trip creation.
     *
     * @param visibility the visibility setting
     * @return a new TripSettings instance
     */
    public TripSettings createTripSettings(TripVisibility visibility) {
        return createTripSettings(TripStatus.CREATED, visibility);
    }

    /**
     * Creates a new TripDetails with null timestamps and locations. Used during trip creation.
     *
     * @return a new TripDetails instance
     */
    public TripDetails createTripDetails() {
        return createDefaultTripDetails();
    }

    /**
     * Ensures TripSettings is initialized with specific visibility if null.
     *
     * @param trip the trip to check
     * @param visibility the visibility to set
     */
    public void ensureTripSettings(Trip trip, TripVisibility visibility) {
        if (trip.getTripSettings() == null) {
            trip.setTripSettings(createTripSettings(TripStatus.CREATED, visibility));
        }
    }

    /**
     * Ensures TripSettings is initialized with specific status if null. Returns the previous status
     * (or CREATED if it was null).
     *
     * @param trip the trip to check
     * @param status the status to set if creating new settings
     * @return the previous status
     */
    public TripStatus ensureTripSettingsAndGetPreviousStatus(Trip trip, TripStatus status) {
        if (trip.getTripSettings() == null) {
            trip.setTripSettings(createTripSettings(status, TripVisibility.PUBLIC));
            return TripStatus.CREATED;
        }
        return trip.getTripSettings().getTripStatus();
    }

    /**
     * Ensures TripDetails is initialized with default values if null.
     *
     * @param trip the trip to check
     */
    public void ensureTripDetails(Trip trip) {
        if (trip.getTripDetails() == null) {
            trip.setTripDetails(createDefaultTripDetails());
        }
    }

    private TripSettings createTripSettings(TripStatus status, TripVisibility visibility) {
        return TripSettings.builder()
                .tripStatus(status)
                .visibility(visibility)
                .updateRefresh(null)
                .build();
    }

    private TripDetails createDefaultTripDetails() {
        return TripDetails.builder()
                .startTimestamp(null)
                .endTimestamp(null)
                .startLocation(null)
                .endLocation(null)
                .build();
    }
}
