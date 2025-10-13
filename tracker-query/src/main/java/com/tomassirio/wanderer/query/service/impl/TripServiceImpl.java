package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.service.TripService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    private final TripMapper tripMapper = TripMapper.INSTANCE;

    @Override
    public TripDTO getTrip(UUID id) {
        return tripRepository
                .findById(id)
                .map(tripMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
    }

    @Override
    public List<TripDTO> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getPublicTrips() {
        return tripRepository.findByVisibility(TripVisibility.PUBLIC).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getTripsForUser(UUID userId) {
        return tripRepository.findByOwnerId(userId).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }
}
