package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.commons.mapper.TripPlanMapper;
import com.tomassirio.wanderer.query.repository.TripPlanRepository;
import com.tomassirio.wanderer.query.service.TripPlanService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripPlanServiceImpl implements TripPlanService {

    private final TripPlanRepository tripPlanRepository;
    private final TripPlanMapper tripPlanMapper = TripPlanMapper.INSTANCE;

    @Override
    public TripPlanDTO getTripPlan(UUID planId) {
        return tripPlanRepository
                .findById(planId)
                .map(tripPlanMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Trip plan not found"));
    }

    @Override
    public List<TripPlanDTO> getTripPlansForUser(UUID userId) {
        return tripPlanRepository.findByUserId(userId).stream()
                .map(tripPlanMapper::toDTO)
                .collect(Collectors.toList());
    }
}
