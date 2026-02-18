package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripUnpromotedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for TripUnpromotedEvent that handles deletion.
 *
 * <p>This handler removes the promoted trip record during the transaction. WebSocket broadcasting
 * is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripUnpromotedEventHandler implements EventHandler<TripUnpromotedEvent> {

    private final PromotedTripRepository promotedTripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(TripUnpromotedEvent event) {
        log.debug("Handling TripUnpromotedEvent for trip: {}", event.getTripId());

        promotedTripRepository.deleteByTripId(event.getTripId());
        log.info("Trip unpromoted: {}", event.getTripId());
    }
}
