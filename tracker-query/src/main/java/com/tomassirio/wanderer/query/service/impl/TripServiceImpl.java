package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.service.TripService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        return tripRepository.findAll().stream().map(tripMapper::toDTO).toList();
    }

    @Override
    public List<TripDTO> getPublicTrips() {
        return tripRepository.findByTripSettingsVisibility(TripVisibility.PUBLIC).stream()
                .map(tripMapper::toDTO)
                .toList();
    }

    @Override
    public List<TripDTO> getTripsForUser(UUID userId) {
        return tripRepository.findByUserId(userId).stream().map(tripMapper::toDTO).toList();
    }

    @Override
    public List<TripDTO> getTripsForUserWithVisibility(UUID userId, UUID requestingUserId) {
        // Check if users are friends
        boolean areFriends =
                requestingUserId != null
                        && friendshipRepository.existsByUserIdAndFriendId(requestingUserId, userId);

        // Determine allowed visibilities based on friendship status
        List<TripVisibility> allowedVisibilities =
                areFriends
                        ? List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED)
                        : List.of(TripVisibility.PUBLIC);

        return tripRepository.findByUserIdAndVisibilityIn(userId, allowedVisibilities).stream()
                .map(tripMapper::toDTO)
                .toList();
    }

    @Override
    public List<TripDTO> getOngoingPublicTrips(UUID requestingUserId) {
        List<Trip> publicTrips =
                tripRepository.findByVisibilityAndStatus(
                        TripVisibility.PUBLIC, TripStatus.IN_PROGRESS);

        if (requestingUserId == null) {
            return publicTrips.stream().map(tripMapper::toDTO).toList();
        }

        // Get followed user IDs
        Set<UUID> followedUserIds =
                userFollowRepository.findByFollowerId(requestingUserId).stream()
                        .map(UserFollow::getFollowedId)
                        .collect(Collectors.toSet());

        Map<Boolean, List<Trip>> partitionedTrips =
                publicTrips.stream()
                        .collect(
                                Collectors.partitioningBy(
                                        trip -> followedUserIds.contains(trip.getUserId())));

        return Stream.concat(
                        partitionedTrips.get(true).stream(), partitionedTrips.get(false).stream())
                .map(tripMapper::toDTO)
                .toList();
    }
}
