package com.tomassirio.wanderer.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomassirio.wanderer.command.dto.LocationUpdateRequest;
import com.tomassirio.wanderer.command.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.command.service.LocationService;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitLocationUpdate_whenValidRequest_shouldReturnCreated() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        LocationUpdateRequest request = new LocationUpdateRequest(TestEntityFactory.LATITUDE, TestEntityFactory.LONGITUDE, null, null, null, null, "test");

        Trip trip = TestEntityFactory.createTrip(tripId);
        Location createdLocation = TestEntityFactory.createLocation(locationId, trip);

        when(locationService.createLocationUpdate(eq(tripId), any(LocationUpdateRequest.class))).thenReturn(createdLocation);

        mockMvc.perform(post("/api/1/{tripId}/location", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(locationId.toString()));
    }

    @Test
    void submitLocationUpdate_whenInvalidLatitude_shouldReturnBadRequest() throws Exception {
        UUID tripId = UUID.randomUUID();
        // Latitude is out of range
        LocationUpdateRequest request = new LocationUpdateRequest(90.1, TestEntityFactory.LONGITUDE, null, null, null, null, "test");

        mockMvc.perform(post("/api/1/{tripId}/location", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitLocationUpdate_whenTripNotFound_shouldReturnBadRequest() throws Exception {
        UUID tripId = UUID.randomUUID();
        LocationUpdateRequest request = new LocationUpdateRequest(TestEntityFactory.LATITUDE, TestEntityFactory.LONGITUDE, null, null, null, null, "test");

        when(locationService.createLocationUpdate(eq(tripId), any(LocationUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("Trip not found"));

        mockMvc.perform(post("/api/1/{tripId}/location", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitLocationUpdate_whenLongitudeIsNull_shouldReturnBadRequest() throws Exception {
        UUID tripId = UUID.randomUUID();
        // Longitude is null
        LocationUpdateRequest request = new LocationUpdateRequest(TestEntityFactory.LATITUDE, null, null, null, null, null, "test");

        mockMvc.perform(post("/api/1/{tripId}/location", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
