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
        TripDTO expectedResponse =
                new TripDTO(
                        tripId,
                        "Summer Road Trip 2025",
                        USER_ID,
                        TripStatus.CREATED,
                        TripVisibility.PUBLIC,
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("Summer Road Trip 2025"))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.tripStatus").value("CREATED"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void createTrip_whenPrivateVisibility_shouldReturnCreatedTrip() throws Exception {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Private Road Trip", TripVisibility.PRIVATE);

        UUID tripId = UUID.randomUUID();
        TripDTO expectedResponse =
                new TripDTO(
                        tripId,
                        "Private Road Trip",
                        USER_ID,
                        TripStatus.CREATED,
                        TripVisibility.PRIVATE,
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void createTrip_whenNameIsTooShort_shouldReturnBadRequest() throws Exception {
        // Given
        TripCreationRequest request = new TripCreationRequest("AB", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
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
                        post("/api/1/trips")
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
                        post("/api/1/trips")
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
                        post("/api/1/trips")
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
        TripDTO expectedResponse =
                new TripDTO(
                        tripId,
                        "Protected Trip",
                        USER_ID,
                        TripStatus.CREATED,
                        TripVisibility.PROTECTED,
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        true);

        doReturn(expectedResponse)
                .when(tripService)
                .createTrip(any(UUID.class), any(TripCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post("/api/1/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("PROTECTED"));
    }

    @Test
    void updateTrip_whenValidRequest_shouldReturnUpdatedTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Updated Trip Name", TripVisibility.PUBLIC);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId,
                        "Updated Trip Name",
                        USER_ID,
                        TripStatus.IN_PROGRESS,
                        TripVisibility.PUBLIC,
                        null,
                        Instant.now(),
                        null,
                        null,
                        Instant.now().minusSeconds(3600),
                        true);

        when(tripService.updateTrip(eq(tripId), any(TripUpdateRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Trip Name"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void updateTrip_whenChangingVisibility_shouldReturnUpdatedTrip() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Trip Name", TripVisibility.PRIVATE);

        TripDTO expectedResponse =
                new TripDTO(
                        tripId,
                        "Trip Name",
                        USER_ID,
                        TripStatus.CREATED,
                        TripVisibility.PRIVATE,
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        true);

        when(tripService.updateTrip(eq(tripId), any(TripUpdateRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void updateTrip_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Updated Trip", TripVisibility.PUBLIC);

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
        TripUpdateRequest request = new TripUpdateRequest("", TripVisibility.PUBLIC);

        // When & Then
        mockMvc.perform(
                        put("/api/1/trips/{id}", tripId)
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
                        put("/api/1/trips/{id}", tripId)
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
