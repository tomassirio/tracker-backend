package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.service.UserFollowQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowQueryServiceImpl implements UserFollowQueryService {

    private final UserFollowRepository userFollowRepository;

    @Override
    public List<UserFollowResponse> getFollowing(UUID userId) {
        log.info("Getting following list for user {}", userId);
        return userFollowRepository.findByFollowerId(userId).stream()
                .map(
                        follow ->
                                new UserFollowResponse(
                                        follow.getId(),
                                        follow.getFollowerId(),
                                        follow.getFollowedId(),
                                        follow.getCreatedAt()))
                .toList();
    }

    @Override
    public List<UserFollowResponse> getFollowers(UUID userId) {
        log.info("Getting followers list for user {}", userId);
        return userFollowRepository.findByFollowedId(userId).stream()
                .map(
                        follow ->
                                new UserFollowResponse(
                                        follow.getId(),
                                        follow.getFollowerId(),
                                        follow.getFollowedId(),
                                        follow.getCreatedAt()))
                .toList();
    }
}
