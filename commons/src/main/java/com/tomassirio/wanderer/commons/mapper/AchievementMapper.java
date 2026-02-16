package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AchievementMapper {

    AchievementMapper INSTANCE = Mappers.getMapper(AchievementMapper.class);

    @Mapping(
            target = "id",
            expression =
                    "java(achievement.getId() != null ? achievement.getId().toString() : null)")
    AchievementDTO toDTO(Achievement achievement);

    @Mapping(
            target = "id",
            expression =
                    "java(achievementDTO.id() != null ? java.util.UUID.fromString(achievementDTO.id()) : null)")
    @Mapping(target = "enabled", ignore = true)
    Achievement toEntity(AchievementDTO achievementDTO);
}
