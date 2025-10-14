package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = LocationMapper.class)
public interface TripMapper {

    TripMapper INSTANCE = Mappers.getMapper(TripMapper.class);

    @Mapping(
            target = "ownerId",
            expression = "java(trip.getOwner() != null ? trip.getOwner().getId() : null)")
    TripDTO toDTO(Trip trip);

    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "messages", ignore = true)
    Trip toEntity(TripDTO tripDTO);
}
