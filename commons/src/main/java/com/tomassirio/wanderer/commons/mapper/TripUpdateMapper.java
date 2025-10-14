package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripUpdateMapper {

    TripUpdateMapper INSTANCE = Mappers.getMapper(TripUpdateMapper.class);

    @Mapping(target = "tripId", source = "trip.id")
    TripUpdateDTO toDTO(TripUpdate tripUpdate);

    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    TripUpdate toEntity(TripUpdateDTO tripUpdateDTO);
}
