package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.UserDetails;
import com.tomassirio.wanderer.commons.dto.UserDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface UserDetailsMapper {

    UserDetailsMapper INSTANCE = Mappers.getMapper(UserDetailsMapper.class);

    UserDetailsDTO toDTO(UserDetails userDetails);

    UserDetails toEntity(UserDetailsDTO userDetailsDTO);
}
