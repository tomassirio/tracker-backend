package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import com.tomassirio.wanderer.commons.mapper.TripUpdateMapper;
import com.tomassirio.wanderer.query.repository.TripUpdateRepository;
import com.tomassirio.wanderer.query.service.TripUpdateService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link TripUpdateService} for querying trip update data.
 *
 * @since 0.4.2
 */
@Service
@AllArgsConstructor
public class TripUpdateServiceImpl implements TripUpdateService {

    private final TripUpdateRepository tripUpdateRepository;
    private final TripUpdateMapper tripUpdateMapper = TripUpdateMapper.INSTANCE;

    @Override
    public TripUpdateDTO getTripUpdate(UUID id) {
        return tripUpdateRepository
                .findById(id)
                .map(tripUpdateMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Trip update not found"));
    }

    @Override
    public List<TripUpdateDTO> getTripUpdatesForTrip(UUID tripId) {
        return tripUpdateRepository.findByTripIdOrderByTimestampDesc(tripId).stream()
                .map(tripUpdateMapper::toDTO)
                .collect(Collectors.toList());
    }
}
