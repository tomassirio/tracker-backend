package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripSettingsUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting trip settings update events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripSettingsUpdatedEvent and updating
 * trip settings in the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripSettingsUpdatedEventHandler implements EventHandler<TripSettingsUpdatedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(TripSettingsUpdatedEvent event) {
        log.debug("Persisting TripSettingsUpdatedEvent for trip: {}", event.getTripId());

        tripRepository
                .findById(event.getTripId())
                .ifPresent(
                        trip -> {
                            embeddedObjectsInitializer.ensureTripSettings(trip, null);
                            if (event.getUpdateRefresh() != null) {
                                trip.getTripSettings().setUpdateRefresh(event.getUpdateRefresh());
                            }
                            if (event.getAutomaticUpdates() != null) {
                                trip.getTripSettings()
                                        .setAutomaticUpdates(event.getAutomaticUpdates());
                            }
                            // No need to call save() - entity is managed and will be flushed
                            // automatically
                            log.info("Trip settings updated: {}", event.getTripId());
                        });
    }
}
