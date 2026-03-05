package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.DonationLinkUpdatedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for DonationLinkUpdatedEvent that handles updates.
 *
 * <p>This handler updates the donation link during the transaction. WebSocket broadcasting is
 * handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DonationLinkUpdatedEventHandler implements EventHandler<DonationLinkUpdatedEvent> {

    private final PromotedTripRepository promotedTripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(DonationLinkUpdatedEvent event) {
        log.debug("Updating donation link for promoted trip: {}", event.getPromotedTripId());

        PromotedTrip promotedTrip =
                promotedTripRepository
                        .findById(event.getPromotedTripId())
                        .orElseThrow(() -> new EntityNotFoundException("Promoted trip not found"));

        promotedTrip.setDonationLink(event.getDonationLink());
        promotedTripRepository.save(promotedTrip);

        log.info("Donation link updated for promoted trip: {}", event.getPromotedTripId());
    }
}
