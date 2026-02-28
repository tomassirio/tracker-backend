package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.CommentReaction;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {

    /**
     * Find a specific reaction by comment and user.
     *
     * @param commentId the comment ID
     * @param userId the user ID
     * @return the reaction if found
     */
    Optional<CommentReaction> findByCommentIdAndUserId(UUID commentId, UUID userId);

    /**
     * Find a specific reaction by comment, user, and reaction type.
     *
     * @param commentId the comment ID
     * @param userId the user ID
     * @param reactionType the reaction type
     * @return the reaction if found
     */
    Optional<CommentReaction> findByCommentIdAndUserIdAndReactionType(
            UUID commentId, UUID userId, ReactionType reactionType);

    /**
     * Delete a specific reaction by comment and user.
     *
     * @param commentId the comment ID
     * @param userId the user ID
     */
    void deleteByCommentIdAndUserId(UUID commentId, UUID userId);
}
