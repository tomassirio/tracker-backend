package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.dto.TripSettingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripSettingsMapper {

    TripSettingsMapper INSTANCE = Mappers.getMapper(TripSettingsMapper.class);

    TripSettingsDTO toDTO(TripSettings tripSettings);

    TripSettings toEntity(TripSettingsDTO tripSettingsDTO);
}
