package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripMapper {

    TripMapper INSTANCE = Mappers.getMapper(TripMapper.class);

    @Mapping(target = "tripStatus", source = "tripSettings.tripStatus")
    @Mapping(target = "visibility", source = "tripSettings.visibility")
    @Mapping(target = "updateRefresh", source = "tripSettings.updateRefresh")
    @Mapping(target = "startTimestamp", source = "tripDetails.startTimestamp")
    @Mapping(target = "endTimestamp", source = "tripDetails.endTimestamp")
    TripDTO toDTO(Trip trip);

    @Mapping(target = "tripSettings.tripStatus", source = "tripStatus")
    @Mapping(target = "tripSettings.visibility", source = "visibility")
    @Mapping(target = "tripSettings.updateRefresh", source = "updateRefresh")
    @Mapping(target = "tripDetails.startTimestamp", source = "startTimestamp")
    @Mapping(target = "tripDetails.endTimestamp", source = "endTimestamp")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "tripUpdates", ignore = true)
    Trip toEntity(TripDTO tripDTO);
}
