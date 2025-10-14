package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TripPlanMapper {

    TripPlanMapper INSTANCE = Mappers.getMapper(TripPlanMapper.class);

    default TripPlanDTO toDTO(TripPlan tripPlan) {
        if (tripPlan == null) {
            return null;
        }

        return new TripPlanDTO(
                tripPlan.getId(),
                tripPlan.getName(),
                tripPlan.getPlanType(),
                tripPlan.getUserId(),
                tripPlan.getCreatedTimestamp(),
                tripPlan.getStartDate(),
                tripPlan.getEndDate(),
                tripPlan.getStartLocation(),
                tripPlan.getEndLocation());
    }
}
