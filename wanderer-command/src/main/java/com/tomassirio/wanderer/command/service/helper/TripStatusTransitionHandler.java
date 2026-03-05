package com.tomassirio.wanderer.command.service.helper;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Helper component responsible for updating trip timestamps based on status transitions. */
@Component
public class TripStatusTransitionHandler {

    /**
     * Updates trip timestamps based on status transitions. Sets startTimestamp when transitioning
     * from CREATED to IN_PROGRESS. Sets endTimestamp when transitioning to FINISHED.
     *
     * @param trip the trip to update
     * @param previousStatus the previous status
     * @param newStatus the new status
     */
    public void handleStatusTransition(Trip trip, TripStatus previousStatus, TripStatus newStatus) {
        if (newStatus == TripStatus.IN_PROGRESS && previousStatus == TripStatus.CREATED) {
            trip.getTripDetails().setStartTimestamp(Instant.now());
        } else if (newStatus == TripStatus.FINISHED) {
            trip.getTripDetails().setEndTimestamp(Instant.now());
        }
    }
}
