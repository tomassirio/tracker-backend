package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.dto.TripDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripDetailsMapper {

    TripDetailsMapper INSTANCE = Mappers.getMapper(TripDetailsMapper.class);

    TripDetailsDTO toDTO(TripDetails tripDetails);

    TripDetails toEntity(TripDetailsDTO tripDetailsDTO);
}
