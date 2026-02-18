package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import com.tomassirio.wanderer.query.dto.PromotedTripResponse;
import com.tomassirio.wanderer.query.repository.PromotedTripRepository;
import com.tomassirio.wanderer.query.service.PromotedTripQueryService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for promoted trip query operations. Handles promoted trip retrieval logic
 * using the promoted trip repository.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotedTripQueryServiceImpl implements PromotedTripQueryService {

    private final PromotedTripRepository promotedTripRepository;

    @Override
    public List<PromotedTripResponse> getAllPromotedTrips() {
        return promotedTripRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PromotedTripResponse getPromotionByTripId(UUID tripId) {
        PromotedTrip promotedTrip =
                promotedTripRepository
                        .findByTripId(tripId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip is not promoted"));
        return mapToResponse(promotedTrip);
    }

    @Override
    public boolean isTripPromoted(UUID tripId) {
        return promotedTripRepository.findByTripId(tripId).isPresent();
    }

    private PromotedTripResponse mapToResponse(PromotedTrip promotedTrip) {
        return new PromotedTripResponse(
                promotedTrip.getId().toString(),
                promotedTrip.getTripId().toString(),
                promotedTrip.getDonationLink(),
                promotedTrip.getPromotedBy().toString(),
                promotedTrip.getPromotedAt());
    }
}
