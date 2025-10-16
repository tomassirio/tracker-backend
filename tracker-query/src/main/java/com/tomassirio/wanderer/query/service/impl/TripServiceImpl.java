package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.service.TripService;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserFollowRepository userFollowRepository;

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
    public List<TripDTO> getTripsForUserWithVisibility(UUID userId, UUID requestingUserId) {
        // Check if users are friends
        boolean areFriends =
                requestingUserId != null
                        && friendshipRepository.existsByUserIdAndFriendId(
                                requestingUserId, userId);

        List<Trip> trips;
        if (areFriends) {
            // Friends can see PUBLIC and PROTECTED trips
            List<TripVisibility> visibilities =
                    List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED);
            trips = tripRepository.findByUserIdAndVisibilityIn(userId, visibilities);
        } else {
            // Non-friends can only see PUBLIC trips
            trips =
                    tripRepository.findByUserId(userId).stream()
                            .filter(
                                    trip ->
                                            trip.getTripSettings().getVisibility()
                                                    == TripVisibility.PUBLIC)
                            .toList();
        }

        return trips.stream().map(tripMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getOngoingPublicTrips(UUID requestingUserId) {
        List<Trip> publicTrips =
                tripRepository.findByVisibilityAndStatus(
                        TripVisibility.PUBLIC, TripStatus.IN_PROGRESS);

        if (requestingUserId == null) {
            return publicTrips.stream().map(tripMapper::toDTO).collect(Collectors.toList());
        }

        // Get followed user IDs
        Set<UUID> followedUserIds =
                userFollowRepository.findByFollowerId(requestingUserId).stream()
                        .map(follow -> follow.getFollowedId())
                        .collect(Collectors.toSet());

        // Separate trips by followed users
        List<Trip> followedTrips = new ArrayList<>();
        List<Trip> otherTrips = new ArrayList<>();

        for (Trip trip : publicTrips) {
            if (followedUserIds.contains(trip.getUserId())) {
                followedTrips.add(trip);
            } else {
                otherTrips.add(trip);
            }
        }

        // Combine with followed trips first
        List<Trip> sortedTrips = new ArrayList<>(followedTrips);
        sortedTrips.addAll(otherTrips);

        return sortedTrips.stream().map(tripMapper::toDTO).collect(Collectors.toList());
    }
}
