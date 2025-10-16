package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Find all top-level comments for a trip (comments with no parent). The replies will be fetched
     * via the Comment entity's relationships.
     */
    @Query(
            "SELECT c FROM Comment c WHERE c.trip.id = :tripId AND c.parentComment IS NULL ORDER BY c.timestamp DESC")
    List<Comment> findTopLevelCommentsByTripId(@Param("tripId") UUID tripId);
}
