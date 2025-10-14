package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper = TripMapper.INSTANCE;

    @Override
    public TripDTO createTrip(UUID ownerId, TripCreationRequest request) {
        User owner =
                userRepository
                        .findById(ownerId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(request.visibility())
                        .updateRefresh(null)
                        .build();

        TripDetails tripDetails =
                TripDetails.builder()
                        .startTimestamp(null)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip trip =
                Trip.builder()
                        .name(request.name())
                        .userId(ownerId)
                        .tripSettings(tripSettings)
                        .tripDetails(tripDetails)
                        .tripPlanId(null)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    public TripDTO updateTrip(UUID id, TripUpdateRequest request) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        trip.setName(request.name());
        trip.getTripSettings().setVisibility(request.visibility());

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    public void deleteTrip(UUID id) {
        tripRepository.deleteById(id);
    }
}
