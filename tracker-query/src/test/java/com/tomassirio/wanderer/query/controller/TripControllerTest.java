package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.LocationDTO;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.query.service.TripService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private MockMvc mockMvc;

    @Mock private TripService tripService;

    @InjectMocks private TripController tripController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(tripController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void getTrip_whenTripExists_shouldReturnTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationDTO startLocation = createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903);
        LocationDTO endLocation = createLocationDTO(UUID.randomUUID(), 37.7749, -122.4194);

        TripDTO trip =
                new TripDTO(
                        tripId,
                        "Summer Road Trip",
                        LocalDate.now().minusDays(5),
                        LocalDate.now().plusDays(10),
                        1250.5,
                        startLocation,
                        endLocation,
                        TripVisibility.PUBLIC);

        when(tripService.getTrip(tripId)).thenReturn(trip);

        // When & Then
        mockMvc.perform(get("/api/1/trips/{id}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("Summer Road Trip"))
                .andExpect(jsonPath("$.totalDistance").value(1250.5))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.startingLocation.latitude").value(39.7392))
                .andExpect(jsonPath("$.startingLocation.longitude").value(-104.9903))
                .andExpect(jsonPath("$.endingLocation.latitude").value(37.7749))
                .andExpect(jsonPath("$.endingLocation.longitude").value(-122.4194));
    }

    @Test
    void getTrip_whenTripDoesNotExist_shouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();

        when(tripService.getTrip(nonExistentTripId))
                .thenThrow(new EntityNotFoundException("Trip not found"));

        // When & Then
        mockMvc.perform(get("/api/1/trips/{id}", nonExistentTripId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrip_whenTripHasNoEndingLocation_shouldReturnTripWithNullEndingLocation()
            throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationDTO startLocation = createLocationDTO(UUID.randomUUID(), 40.7128, -74.0060);

        TripDTO trip =
                new TripDTO(
                        tripId,
                        "One Way Trip",
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(5),
                        500.0,
                        startLocation,
                        null, // No ending location
                        TripVisibility.PRIVATE);

        when(tripService.getTrip(tripId)).thenReturn(trip);

        // When & Then
        mockMvc.perform(get("/api/1/trips/{id}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("One Way Trip"))
                .andExpect(jsonPath("$.startingLocation").exists())
                .andExpect(jsonPath("$.endingLocation").isEmpty());
    }

    @Test
    void getTrip_whenLocationHasNoAltitude_shouldReturnTripWithNullAltitude() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationDTO startLocation = createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903, null);
        LocationDTO endLocation = createLocationDTO(UUID.randomUUID(), 37.7749, -122.4194, null);

        TripDTO trip =
                new TripDTO(
                        tripId,
                        "Flat Trip",
                        LocalDate.now(),
                        LocalDate.now().plusDays(3),
                        200.0,
                        startLocation,
                        endLocation,
                        TripVisibility.PUBLIC);

        when(tripService.getTrip(tripId)).thenReturn(trip);

        // When & Then
        mockMvc.perform(get("/api/1/trips/{id}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.startingLocation.altitude").isEmpty())
                .andExpect(jsonPath("$.endingLocation.altitude").isEmpty());
    }

    @Test
    void getAllTrips_whenTripsExist_shouldReturnListOfTrips() throws Exception {
        // Given
        UUID tripId1 = UUID.randomUUID();
        UUID tripId2 = UUID.randomUUID();

        LocationDTO location1 = createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903);
        LocationDTO location2 = createLocationDTO(UUID.randomUUID(), 40.7128, -74.0060);

        TripDTO trip1 =
                new TripDTO(
                        tripId1,
                        "Trip 1",
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(5),
                        500.0,
                        location1,
                        location2,
                        TripVisibility.PUBLIC);

        TripDTO trip2 =
                new TripDTO(
                        tripId2,
                        "Trip 2",
                        LocalDate.now().minusDays(3),
                        LocalDate.now().plusDays(2),
                        300.0,
                        location2,
                        location1,
                        TripVisibility.PRIVATE);

        when(tripService.getAllTrips()).thenReturn(List.of(trip1, trip2));

        // When & Then
        mockMvc.perform(get("/api/1/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(tripId1.toString()))
                .andExpect(jsonPath("$[0].name").value("Trip 1"))
                .andExpect(jsonPath("$[0].totalDistance").value(500.0))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"))
                .andExpect(jsonPath("$[1].id").value(tripId2.toString()))
                .andExpect(jsonPath("$[1].name").value("Trip 2"))
                .andExpect(jsonPath("$[1].totalDistance").value(300.0))
                .andExpect(jsonPath("$[1].visibility").value("PRIVATE"));
    }

    @Test
    void getAllTrips_whenNoTripsExist_shouldReturnEmptyList() throws Exception {
        // Given
        when(tripService.getAllTrips()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/1/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllTrips_withMultipleTrips_shouldReturnAllTrips() throws Exception {
        // Given
        List<TripDTO> trips =
                List.of(
                        createTripDTO(UUID.randomUUID(), "Trip A"),
                        createTripDTO(UUID.randomUUID(), "Trip B"),
                        createTripDTO(UUID.randomUUID(), "Trip C"),
                        createTripDTO(UUID.randomUUID(), "Trip D"),
                        createTripDTO(UUID.randomUUID(), "Trip E"));

        when(tripService.getAllTrips()).thenReturn(trips);

        // When & Then
        mockMvc.perform(get("/api/1/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    // Helper methods to create test data
    private LocationDTO createLocationDTO(UUID id, double latitude, double longitude) {
        return createLocationDTO(id, latitude, longitude, 1500.0);
    }

    private LocationDTO createLocationDTO(
            UUID id, double latitude, double longitude, Double altitude) {
        return new LocationDTO(
                id, latitude, longitude, Instant.now(), altitude, 10.0, 85, "TEST_SOURCE");
    }

    private TripDTO createTripDTO(UUID tripId, String name) {
        LocationDTO startLocation = createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903);
        LocationDTO endLocation = createLocationDTO(UUID.randomUUID(), 37.7749, -122.4194);

        return new TripDTO(
                tripId,
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                1000.0,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC);
    }
}
