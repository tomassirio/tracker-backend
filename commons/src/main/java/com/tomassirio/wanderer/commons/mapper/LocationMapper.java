package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.dto.LocationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    LocationDTO toDTO(Location location);

    Location toEntity(LocationDTO locationDTO);
}
