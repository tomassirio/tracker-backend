package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.BaseAchievement;
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
    @Mapping(target = "category", expression = "java(achievement.getCategory())")
    AchievementDTO toDTO(BaseAchievement achievement);
}
