package com.tomassirio.wanderer.query.controller;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
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
class UserAchievementQueryControllerTest {

    private static final String USERS_URL = "/api/1/users";

    private MockMvc mockMvc;

    @Mock private AchievementQueryService achievementQueryService;

    @InjectMocks private UserAchievementQueryController userAchievementQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        userAchievementQueryController, new GlobalExceptionHandler());
    }

    @Test
    void getMyAchievements_shouldReturnCurrentUserAchievements() throws Exception {
        // Given
        AchievementDTO achievementDTO =
                new AchievementDTO(
                        UUID.randomUUID().toString(),
                        AchievementType.UPDATES_10,
                        "Getting Started",
                        "Post 10 updates",
                        10);

        UserAchievementDTO userAchievement =
                new UserAchievementDTO(
                        UUID.randomUUID().toString(),
                        USER_ID.toString(),
                        achievementDTO,
                        UUID.randomUUID().toString(),
                        Instant.now(),
                        15.0);

        when(achievementQueryService.getUserAchievements(USER_ID))
                .thenReturn(List.of(userAchievement));

        // When & Then
        mockMvc.perform(get(USERS_URL + "/me/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$[0].achievement.type").value("UPDATES_10"));
    }

    @Test
    void getUserAchievements_shouldReturnUserAchievements() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

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

        when(achievementQueryService.getUserAchievements(userId))
                .thenReturn(List.of(userAchievement));

        // When & Then
        mockMvc.perform(get(USERS_URL + "/" + userId + "/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].tripId").value(tripId.toString()))
                .andExpect(jsonPath("$[0].achievement.type").value("DISTANCE_100KM"))
                .andExpect(jsonPath("$[0].valueAchieved").value(105.5));
    }
}
