package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.TripPlanUpdatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class TripPlanUpdatedEventPersistenceHandler implements EventHandler<TripPlanUpdatedEvent> {

    private final TripPlanRepository tripPlanRepository;
    private final TripPlanMetadataProcessor metadataProcessor;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripPlanUpdatedEvent event) {
        log.debug("Persisting TripPlanUpdatedEvent for trip plan: {}", event.getTripPlanId());

        TripPlan tripPlan =
                tripPlanRepository
                        .findById(event.getTripPlanId())
                        .orElseThrow(() -> new EntityNotFoundException("Trip plan not found"));

        tripPlan.setName(event.getName());
        tripPlan.setStartDate(event.getStartDate());
        tripPlan.setEndDate(event.getEndDate());
        tripPlan.setStartLocation(event.getStartLocation());
        tripPlan.setEndLocation(event.getEndLocation());
        tripPlan.setWaypoints(event.getWaypoints() != null ? event.getWaypoints() : List.of());

        // Re-validate metadata for the plan type
        metadataProcessor.applyMetadata(tripPlan, tripPlan.getMetadata());

        tripPlanRepository.save(tripPlan);
        log.info("Trip plan updated and persisted: {}", event.getTripPlanId());
    }
}
