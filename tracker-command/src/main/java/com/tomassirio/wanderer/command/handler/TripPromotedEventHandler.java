package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripPromotedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for TripPromotedEvent that handles persistence.
 *
 * <p>This handler persists the promoted trip during the transaction. WebSocket broadcasting is
 * handled centrally by {@link com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripPromotedEventHandler implements EventHandler<TripPromotedEvent> {

    private final PromotedTripRepository promotedTripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(TripPromotedEvent event) {
        log.debug("Persisting TripPromotedEvent for trip: {}", event.getTripId());

        PromotedTrip promotedTrip =
                PromotedTrip.builder()
                        .id(event.getId())
                        .tripId(event.getTripId())
                        .donationLink(event.getDonationLink())
                        .promotedBy(event.getPromotedBy())
                        .promotedAt(event.getPromotedAt())
                        .build();

        promotedTripRepository.save(promotedTrip);
        log.info("Trip promoted and persisted: {}", event.getTripId());
    }
}
