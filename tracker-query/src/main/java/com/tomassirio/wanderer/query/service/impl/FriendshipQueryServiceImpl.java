package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.FriendshipResponse;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.service.FriendshipQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipQueryServiceImpl implements FriendshipQueryService {

    private final FriendshipRepository friendshipRepository;

    @Override
    public List<FriendshipResponse> getFriends(UUID userId) {
        log.info("Getting friends for user {}", userId);
        return friendshipRepository.findByUserId(userId).stream()
                .map(
                        friendship ->
                                new FriendshipResponse(
                                        friendship.getUserId(), friendship.getFriendId()))
                .toList();
    }
}
