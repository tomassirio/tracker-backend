package com.tomassirio.wanderer.command.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.command.service.TripPlanPolylineService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
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
class AdminTripPlanControllerTest {

    private static final String ADMIN_TRIP_PLANS_URL = "/api/1/admin/trip-plans";

    private MockMvc mockMvc;

    @Mock private TripPlanPolylineService tripPlanPolylineService;

    @InjectMocks private AdminTripPlanController adminTripPlanController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(adminTripPlanController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void recomputePolyline_shouldReturnNoContent() throws Exception {
        UUID tripPlanId = UUID.randomUUID();
        doNothing().when(tripPlanPolylineService).computePolyline(tripPlanId);

        mockMvc.perform(post(ADMIN_TRIP_PLANS_URL + "/{tripPlanId}/recompute-polyline", tripPlanId))
                .andExpect(status().isNoContent());

        verify(tripPlanPolylineService).computePolyline(tripPlanId);
    }

    @Test
    void recomputePolyline_whenTripPlanNotFound_shouldReturnNotFound() throws Exception {
        UUID tripPlanId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Trip plan not found: " + tripPlanId))
                .when(tripPlanPolylineService)
                .computePolyline(tripPlanId);

        mockMvc.perform(post(ADMIN_TRIP_PLANS_URL + "/{tripPlanId}/recompute-polyline", tripPlanId))
                .andExpect(status().isNotFound());
    }

    @Test
    void recomputePolyline_whenServiceFails_shouldReturnInternalServerError() throws Exception {
        UUID tripPlanId = UUID.randomUUID();
        doThrow(new RuntimeException("Google API unavailable"))
                .when(tripPlanPolylineService)
                .computePolyline(tripPlanId);

        mockMvc.perform(post(ADMIN_TRIP_PLANS_URL + "/{tripPlanId}/recompute-polyline", tripPlanId))
                .andExpect(status().isInternalServerError());
    }
}
