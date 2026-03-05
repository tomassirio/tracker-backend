package com.tomassirio.wanderer.command.websocket.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AchievementUnlockedPayload {
    private UUID achievementId;
    private AchievementType achievementType;
    private String achievementName;
    private UUID tripId;
    private Double valueAchieved;
    private Instant unlockedAt;
}
