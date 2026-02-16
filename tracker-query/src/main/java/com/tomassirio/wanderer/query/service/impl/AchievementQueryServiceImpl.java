package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.dto.UnlockedAchievementDTO;
import com.tomassirio.wanderer.commons.mapper.AchievementMapper;
import com.tomassirio.wanderer.commons.mapper.UnlockedAchievementMapper;
import com.tomassirio.wanderer.query.repository.AchievementRepository;
import com.tomassirio.wanderer.query.repository.UserAchievementRepository;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for achievement query operations. Handles achievement retrieval logic
 * using the achievement repositories.
 */
@Service
@RequiredArgsConstructor
public class AchievementQueryServiceImpl implements AchievementQueryService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Override
    public List<AchievementDTO> getAvailableAchievements() {
        return achievementRepository.findByEnabledTrue().stream()
                .map(AchievementMapper.INSTANCE::toDTO)
                .toList();
    }

    @Override
    public List<UnlockedAchievementDTO> getUserAchievements(UUID userId) {
        return userAchievementRepository.findByUserId(userId).stream()
                .map(UnlockedAchievementMapper.INSTANCE::toDTO)
                .toList();
    }

    @Override
    public List<UnlockedAchievementDTO> getUserAchievementsByTrip(UUID userId, UUID tripId) {
        return userAchievementRepository.findByUserIdAndTripId(userId, tripId).stream()
                .map(UnlockedAchievementMapper.INSTANCE::toDTO)
                .toList();
    }
}
