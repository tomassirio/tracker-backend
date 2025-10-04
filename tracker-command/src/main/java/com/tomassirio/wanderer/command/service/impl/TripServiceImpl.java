package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.LocationRequest;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
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

    private final TripMapper tripMapper = TripMapper.INSTANCE;

    @Override
    public TripDTO createTrip(TripCreationRequest request) {
        Trip trip = buildTripFromRequest(request);
        setLocationsOnTrip(trip, request.startingLocation(), request.endingLocation());
        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    public TripDTO updateTrip(UUID id, TripUpdateRequest request) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        updateTripFields(trip, request);
        setLocationsOnTrip(trip, request.startingLocation(), request.endingLocation());

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    public void deleteTrip(UUID id) {
        tripRepository.deleteById(id);
    }

    private Trip buildTripFromRequest(TripCreationRequest request) {
        return Trip.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalDistance(request.totalDistance())
                .visibility(request.visibility())
                .build();
    }

    private void updateTripFields(Trip trip, TripUpdateRequest request) {
        trip.setName(request.name());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setTotalDistance(request.totalDistance());
        trip.setVisibility(request.visibility());
    }

    private void setLocationsOnTrip(
            Trip trip, LocationRequest startingLocation, LocationRequest endingLocation) {
        if (startingLocation != null) {
            trip.setStartingLocation(createLocationFromRequest(startingLocation, trip));
        }

        if (endingLocation != null) {
            trip.setEndingLocation(createLocationFromRequest(endingLocation, trip));
        }
    }

    private Location createLocationFromRequest(LocationRequest request, Trip trip) {
        return Location.builder()
                .latitude(request.latitude())
                .longitude(request.longitude())
                .altitude(request.altitude())
                .timestamp(Instant.now())
                .trip(trip)
                .source("TRIP_ENDPOINT")
                .build();
    }
}
