package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.UserAchievement;
import com.tomassirio.wanderer.commons.dto.UserAchievementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AchievementMapper.class})
public interface UserAchievementMapper {

    UserAchievementMapper INSTANCE = Mappers.getMapper(UserAchievementMapper.class);

    @Mapping(
            target = "id",
            expression =
                    "java(userAchievement.getId() != null ? userAchievement.getId().toString() : null)")
    @Mapping(
            target = "userId",
            expression =
                    "java(userAchievement.getUser() != null && userAchievement.getUser().getId() != null ? userAchievement.getUser().getId().toString() : null)")
    @Mapping(source = "achievement", target = "achievement")
    @Mapping(
            target = "tripId",
            expression =
                    "java(userAchievement.getTrip() != null && userAchievement.getTrip().getId() != null ? userAchievement.getTrip().getId().toString() : null)")
    UserAchievementDTO toDTO(UserAchievement userAchievement);
}
