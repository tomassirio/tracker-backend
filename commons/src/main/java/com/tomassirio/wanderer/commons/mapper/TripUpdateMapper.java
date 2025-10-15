package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripUpdateMapper {

    TripUpdateMapper INSTANCE = Mappers.getMapper(TripUpdateMapper.class);

    @Mapping(
            target = "id",
            expression = "java(tripUpdate.getId() != null ? tripUpdate.getId().toString() : null)")
    @Mapping(
            target = "tripId",
            expression =
                    "java(tripUpdate.getTrip() != null && tripUpdate.getTrip().getId() != null ? tripUpdate.getTrip().getId().toString() : null)")
    TripUpdateDTO toDTO(TripUpdate tripUpdate);

    @Mapping(
            target = "id",
            expression =
                    "java(tripUpdateDTO.id() != null ? java.util.UUID.fromString(tripUpdateDTO.id()) : null)")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    TripUpdate toEntity(TripUpdateDTO tripUpdateDTO);
}
