package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.UnlockedAchievement;
import com.tomassirio.wanderer.commons.dto.UnlockedAchievementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AchievementMapper.class})
public interface UnlockedAchievementMapper {

    UnlockedAchievementMapper INSTANCE = Mappers.getMapper(UnlockedAchievementMapper.class);

    @Mapping(
            target = "id",
            expression =
                    "java(unlockedAchievement.getId() != null ? unlockedAchievement.getId().toString() : null)")
    @Mapping(
            target = "userId",
            expression =
                    "java(unlockedAchievement.getUser() != null && unlockedAchievement.getUser().getId() != null ? unlockedAchievement.getUser().getId().toString() : null)")
    @Mapping(source = "achievement", target = "achievement")
    @Mapping(
            target = "tripId",
            expression =
                    "java(unlockedAchievement.getTrip() != null && unlockedAchievement.getTrip().getId() != null ? unlockedAchievement.getTrip().getId().toString() : null)")
    UnlockedAchievementDTO toDTO(UnlockedAchievement unlockedAchievement);
}
