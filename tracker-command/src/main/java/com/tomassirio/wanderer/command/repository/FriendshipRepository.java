package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.Friendship;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    Optional<Friendship> findByUserIdAndFriendId(UUID userId, UUID friendId);

    List<Friendship> findByUserId(UUID userId);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);
}
