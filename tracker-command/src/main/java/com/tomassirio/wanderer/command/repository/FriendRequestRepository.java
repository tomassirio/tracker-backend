package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(
            UUID senderId, UUID receiverId, FriendRequestStatus status);

    void deleteBySenderIdOrReceiverId(UUID senderId, UUID receiverId);
}
