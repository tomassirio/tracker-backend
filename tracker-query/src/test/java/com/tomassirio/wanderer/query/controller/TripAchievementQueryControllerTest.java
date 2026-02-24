package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.dto.UserAchievementDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class TripAchievementQueryControllerTest {

    private static final String TRIPS_URL = "/api/1/trips";

    private MockMvc mockMvc;

    @Mock private AchievementQueryService achievementQueryService;

    @InjectMocks private TripAchievementQueryController tripAchievementQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        tripAchievementQueryController, new GlobalExceptionHandler());
    }

    @Test
    void getTripAchievements_shouldReturnAllTripAchievements() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AchievementDTO achievementDTO =
                new AchievementDTO(
                        UUID.randomUUID().toString(),
                        AchievementType.DISTANCE_100KM,
                        "First Century",
                        "Walk 100km in a single trip",
                        100);

        UserAchievementDTO userAchievement =
                new UserAchievementDTO(
                        UUID.randomUUID().toString(),
                        userId.toString(),
                        achievementDTO,
                        tripId.toString(),
                        Instant.now(),
                        105.5);

        when(achievementQueryService.getTripAchievements(tripId))
                .thenReturn(List.of(userAchievement));

        // When & Then
        mockMvc.perform(get(TRIPS_URL + "/" + tripId + "/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].tripId").value(tripId.toString()))
                .andExpect(jsonPath("$[0].achievement.type").value("DISTANCE_100KM"))
                .andExpect(jsonPath("$[0].valueAchieved").value(105.5));
    }
}
