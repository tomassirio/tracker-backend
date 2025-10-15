package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.TripStatus;
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
        return tripRepository.findByTripSettingsVisibility(TripVisibility.PUBLIC).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getTripsForUser(UUID userId) {
        return tripRepository.findByUserId(userId).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getTripsForUserWithVisibility(UUID userId) {
        List<TripVisibility> visibilities =
                List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED);
        return tripRepository.findByUserIdAndVisibilityIn(userId, visibilities).stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getOngoingPublicTrips() {
        return tripRepository
                .findByVisibilityAndStatus(TripVisibility.PUBLIC, TripStatus.IN_PROGRESS)
                .stream()
                .map(tripMapper::toDTO)
                .collect(Collectors.toList());
    }
}
