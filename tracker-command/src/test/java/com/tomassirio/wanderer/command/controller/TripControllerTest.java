package com.tomassirio.wanderer.command.controller;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.dto.TripDetailsDTO;
import com.tomassirio.wanderer.commons.dto.TripSettingsDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private static final String TRIPS_BASE_URL = "/api/1/trips";
    private static final String TRIP_BY_ID_URL = TRIPS_BASE_URL + "/{id}";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock private TripService tripService;

    @InjectMocks private TripController tripController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Use shared test utility from commons to register @CurrentUserId resolver
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        tripController, new GlobalExceptionHandler());
    }

    @Test
    void createTrip_whenValidRequest_shouldReturnCreatedTrip() throws Exception {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Summer Road Trip 2025", TripVisibility.PUBLIC);

        UUID tripId = UUID.randomUUID();
        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.CREATED, TripVisibility.PUBLIC, null);
        TripDetailsDTO tripDetails = new TripDetailsDTO(null, null, null, null);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId.toString(),
                        "Summer Road Trip 2025",
                        USER_ID.toString(),
                        tripSettings,
                        tripDetails,
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("Summer Road Trip 2025"))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.tripSettings.tripStatus").value(TripStatus.CREATED.name()))
                .andExpect(
                        jsonPath("$.tripSettings.visibility").value(TripVisibility.PUBLIC.name()))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void createTrip_whenPrivateVisibility_shouldReturnCreatedTrip() throws Exception {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Private Road Trip", TripVisibility.PRIVATE);

        UUID tripId = UUID.randomUUID();
        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.CREATED, TripVisibility.PRIVATE, null);
        TripDetailsDTO tripDetails = new TripDetailsDTO(null, null, null, null);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId.toString(),
                        "Private Road Trip",
                        USER_ID.toString(),
                        tripSettings,
                        tripDetails,
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(
                        jsonPath("$.tripSettings.visibility").value(TripVisibility.PRIVATE.name()));
    }

    @Test
    void createTrip_whenNameIsTooShort_shouldReturnBadRequest() throws Exception {
        // Given
        TripCreationRequest request = new TripCreationRequest("AB", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenNameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        TripCreationRequest request = new TripCreationRequest("", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenNameIsTooLong_shouldReturnBadRequest() throws Exception {
        // Given - name with more than 100 characters
        String longName = "A".repeat(101);
        TripCreationRequest request = new TripCreationRequest(longName, TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenVisibilityIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        TripCreationRequest request = new TripCreationRequest("Summer Road Trip", null);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_whenProtectedVisibility_shouldReturnCreatedTrip() throws Exception {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Protected Trip", TripVisibility.PROTECTED);

        UUID tripId = UUID.randomUUID();
        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.CREATED, TripVisibility.PROTECTED, null);
        TripDetailsDTO tripDetails = new TripDetailsDTO(null, null, null, null);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId.toString(),
                        "Protected Trip",
                        USER_ID.toString(),
                        tripSettings,
                        tripDetails,
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.tripSettings.visibility")
                                .value(TripVisibility.PROTECTED.name()));
    }

    @Test
    void updateTrip_whenValidRequest_shouldReturnUpdatedTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Updated Trip Name", TripVisibility.PUBLIC);

        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.IN_PROGRESS, TripVisibility.PUBLIC, null);
        TripDetailsDTO tripDetails = new TripDetailsDTO(Instant.now(), null, null, null);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId.toString(),
                        "Updated Trip Name",
                        USER_ID.toString(),
                        tripSettings,
                        tripDetails,
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        Instant.now().minusSeconds(3600),
                        true);

        when(tripService.updateTrip(any(UUID.class), eq(tripId), any(TripUpdateRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Trip Name"))
                .andExpect(
                        jsonPath("$.tripSettings.visibility").value(TripVisibility.PUBLIC.name()));
    }

    @Test
    void updateTrip_whenChangingVisibility_shouldReturnUpdatedTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Trip Name", TripVisibility.PRIVATE);

        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.CREATED, TripVisibility.PRIVATE, null);
        TripDetailsDTO tripDetails = new TripDetailsDTO(null, null, null, null);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId.toString(),
                        "Trip Name",
                        USER_ID.toString(),
                        tripSettings,
                        tripDetails,
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        Instant.now(),
                        true);

        when(tripService.updateTrip(any(UUID.class), eq(tripId), any(TripUpdateRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.tripSettings.visibility").value(TripVisibility.PRIVATE.name()));
    }

    @Test
    void updateTrip_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Updated Trip", TripVisibility.PUBLIC);

        when(tripService.updateTrip(any(UUID.class), eq(tripId), any(TripUpdateRequest.class)))
                .thenThrow(new EntityNotFoundException("Trip not found"));

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrip_whenNameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request = new TripUpdateRequest("", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrip_whenNameIsTooShort_shouldReturnBadRequest() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request = new TripUpdateRequest("AB", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrip_whenVisibilityIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request = new TripUpdateRequest("Valid Trip Name", null);

        // When & Then
        mockMvc.perform(
                        put(TRIP_BY_ID_URL, tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrip_whenTripExists_shouldReturnNoContent() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        doNothing().when(tripService).deleteTrip(any(UUID.class), eq(tripId));

        // When & Then
        mockMvc.perform(delete(TRIP_BY_ID_URL, tripId)).andExpect(status().isNoContent());
    }

    @Test
    void deleteTrip_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Trip not found"))
                .when(tripService)
                .deleteTrip(any(UUID.class), eq(tripId));

        // When & Then
        mockMvc.perform(delete(TRIP_BY_ID_URL, tripId)).andExpect(status().isNotFound());
    }
}
