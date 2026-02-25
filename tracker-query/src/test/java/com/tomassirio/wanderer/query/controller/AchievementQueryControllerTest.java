package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import java.util.Arrays;
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
class AchievementQueryControllerTest {

    private static final String ACHIEVEMENTS_URL = "/api/1/achievements";

    private MockMvc mockMvc;

    @Mock private AchievementQueryService achievementQueryService;

    @InjectMocks private AchievementQueryController achievementQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        achievementQueryController, new GlobalExceptionHandler());
    }

    @Test
    void getAvailableAchievements_shouldReturnAllAchievements() throws Exception {
        // Given
        AchievementDTO achievement1 =
                new AchievementDTO(
                        UUID.randomUUID().toString(),
                        AchievementType.DISTANCE_100KM,
                        "First Century",
                        "Walk 100km in a single trip",
                        100);

        AchievementDTO achievement2 =
                new AchievementDTO(
                        UUID.randomUUID().toString(),
                        AchievementType.UPDATES_10,
                        "Getting Started",
                        "Post 10 updates on a single trip",
                        10);

        List<AchievementDTO> achievements = Arrays.asList(achievement1, achievement2);

        when(achievementQueryService.getAvailableAchievements()).thenReturn(achievements);

        // When & Then
        mockMvc.perform(get(ACHIEVEMENTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DISTANCE_100KM"))
                .andExpect(jsonPath("$[0].name").value("First Century"))
                .andExpect(jsonPath("$[0].thresholdValue").value(100))
                .andExpect(jsonPath("$[1].type").value("UPDATES_10"))
                .andExpect(jsonPath("$[1].name").value("Getting Started"));
    }
}
