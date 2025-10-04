package com.tomassirio.wanderer.command.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.dto.LocationRequest;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.LocationDTO;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock private TripService tripService;

    @InjectMocks private TripController tripController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc =
                MockMvcBuilders.standaloneSetup(tripController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void createTrip_whenValidRequest_shouldReturnCreatedTrip() throws Exception {
        // Given
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(39.7392, -104.9903, 1609.3);
        LocationRequest endLocation =
                TestEntityFactory.createLocationRequest(37.7749, -122.4194, 16.0);
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Summer Road Trip 2025", startLocation, endLocation);

        UUID tripId = UUID.randomUUID();
        LocationDTO startLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903, 1609.3);
        LocationDTO endLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 37.7749, -122.4194, 16.0);
        TripDTO expectedResponse =
                TestEntityFactory.createTripDTO(
                        tripId, "Summer Road Trip 2025", startLocationDTO, endLocationDTO);

        when(tripService.createTrip(any(TripCreationRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()));
    }

    @Test
    void createTrip_whenLocationRequestsHaveNoAltitude_shouldReturnCreatedTrip() throws Exception {
        // Given - locations without altitude
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(39.7392, -104.9903, null);
        LocationRequest endLocation =
                TestEntityFactory.createLocationRequest(37.7749, -122.4194, null);
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Road Trip Without Altitude", startLocation, endLocation);

        UUID tripId = UUID.randomUUID();
        LocationDTO startLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 39.7392, -104.9903, null);
        LocationDTO endLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 37.7749, -122.4194, null);
        TripDTO expectedResponse =
                TestEntityFactory.createTripDTO(
                        tripId, "Road Trip Without Altitude", startLocationDTO, endLocationDTO);

        when(tripService.createTrip(any(TripCreationRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()));
    }

    @Test
    void createTrip_whenNameIsTooShort_shouldReturnBadRequest() throws Exception {
        // Given
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();

        TripCreationRequest request =
                new TripCreationRequest(
                        "AB", // Too short (min 3 chars)
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 6, 15),
                        null,
                        startLocation,
                        null,
                        TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenStartingLocationLatitudeIsInvalid_shouldReturnBadRequest()
            throws Exception {
        // Given - latitude out of range
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(91.0, -104.9903, null);

        TripCreationRequest request =
                new TripCreationRequest(
                        "Summer Road Trip",
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 6, 15),
                        null,
                        startLocation,
                        null,
                        TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenStartingLocationLongitudeIsInvalid_shouldReturnBadRequest()
            throws Exception {
        // Given - longitude out of range
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(39.7392, -181.0, null);

        TripCreationRequest request =
                new TripCreationRequest(
                        "Summer Road Trip",
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 6, 15),
                        null,
                        startLocation,
                        null,
                        TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenStartingLocationIsNull_shouldReturnBadRequest() throws Exception {
        // Given - no starting location
        TripCreationRequest request =
                new TripCreationRequest(
                        "Summer Road Trip",
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 6, 15),
                        null,
                        null, // Starting location is required
                        null,
                        TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenVisibilityIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();

        TripCreationRequest request =
                new TripCreationRequest(
                        "Summer Road Trip",
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 6, 15),
                        null,
                        startLocation,
                        null,
                        null); // Visibility is required

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrip_whenValidRequest_shouldReturnUpdatedTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(40.7128, -74.0060, 10.0);
        LocationRequest endLocation =
                TestEntityFactory.createLocationRequest(34.0522, -118.2437, 71.0);
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Updated Trip Name", startLocation, endLocation);

        LocationDTO startLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 40.7128, -74.0060, 10.0);
        LocationDTO endLocationDTO =
                TestEntityFactory.createLocationDTO(UUID.randomUUID(), 34.0522, -118.2437, 71.0);
        TripDTO expectedResponse =
                TestEntityFactory.createTripDTO(
                        tripId, "Updated Trip Name", startLocationDTO, endLocationDTO);

        when(tripService.updateTrip(eq(tripId), any(TripUpdateRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()));
    }

    @Test
    void updateTrip_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();

        TripUpdateRequest request =
                new TripUpdateRequest(
                        "Updated Trip",
                        LocalDate.of(2025, 7, 1),
                        LocalDate.of(2025, 7, 15),
                        null,
                        startLocation,
                        null,
                        TripVisibility.PUBLIC);

        when(tripService.updateTrip(eq(tripId), any(TripUpdateRequest.class)))
                .thenThrow(new EntityNotFoundException("Trip not found"));

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrip_whenNameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();

        TripUpdateRequest request =
                new TripUpdateRequest(
                        "", // Blank name
                        LocalDate.of(2025, 7, 1),
                        LocalDate.of(2025, 7, 15),
                        null,
                        startLocation,
                        null,
                        TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrip_whenTripExists_shouldReturnNoContent() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        doNothing().when(tripService).deleteTrip(tripId);

        // When & Then
        mockMvc.perform(delete("/api/1/trips/{id}", tripId)).andExpect(status().isNoContent());
    }

    @Test
    void deleteTrip_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Trip not found")).when(tripService).deleteTrip(tripId);

        // When & Then
        mockMvc.perform(delete("/api/1/trips/{id}", tripId)).andExpect(status().isNotFound());
    }
}
