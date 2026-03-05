package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.dto.TripMaintenanceStatsDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
import com.tomassirio.wanderer.query.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class AdminTripQueryControllerTest {

    private static final String ADMIN_TRIPS_STATS_URL = "/api/1/admin/trips/stats";

    private MockMvc mockMvc;

    @Mock private TripService tripService;

    @InjectMocks private AdminTripQueryController adminTripQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        adminTripQueryController, new GlobalExceptionHandler());
    }

    @Test
    void getTripMaintenanceStats_shouldReturnStats() throws Exception {
        // Given
        TripMaintenanceStatsDTO stats = new TripMaintenanceStatsDTO(5, 3, 4, 1, 28, 20, 8);

        when(tripService.getTripMaintenanceStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get(ADMIN_TRIPS_STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(5))
                .andExpect(jsonPath("$.tripsWithPolyline").value(3))
                .andExpect(jsonPath("$.tripsWithMultipleLocations").value(4))
                .andExpect(jsonPath("$.tripsMissingPolyline").value(1))
                .andExpect(jsonPath("$.totalUpdates").value(28))
                .andExpect(jsonPath("$.updatesWithGeocoding").value(20))
                .andExpect(jsonPath("$.updatesMissingGeocoding").value(8));
    }

    @Test
    void getTripMaintenanceStats_whenNoTrips_shouldReturnZeroStats() throws Exception {
        // Given
        TripMaintenanceStatsDTO stats = new TripMaintenanceStatsDTO(0, 0, 0, 0, 0, 0, 0);

        when(tripService.getTripMaintenanceStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get(ADMIN_TRIPS_STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(0))
                .andExpect(jsonPath("$.tripsWithPolyline").value(0))
                .andExpect(jsonPath("$.tripsWithMultipleLocations").value(0))
                .andExpect(jsonPath("$.tripsMissingPolyline").value(0))
                .andExpect(jsonPath("$.totalUpdates").value(0))
                .andExpect(jsonPath("$.updatesWithGeocoding").value(0))
                .andExpect(jsonPath("$.updatesMissingGeocoding").value(0));
    }

    @Test
    void getTripMaintenanceStats_whenAllGeocodedAndPolylined_shouldReflectFullCoverage()
            throws Exception {
        // Given
        TripMaintenanceStatsDTO stats = new TripMaintenanceStatsDTO(3, 3, 3, 0, 15, 15, 0);

        when(tripService.getTripMaintenanceStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get(ADMIN_TRIPS_STATS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(3))
                .andExpect(jsonPath("$.tripsWithPolyline").value(3))
                .andExpect(jsonPath("$.tripsMissingPolyline").value(0))
                .andExpect(jsonPath("$.updatesWithGeocoding").value(15))
                .andExpect(jsonPath("$.updatesMissingGeocoding").value(0));
    }
}
