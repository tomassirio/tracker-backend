package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentReactionRepository;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.CommentReaction;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting comment reaction events to the database.
 *
 * <p>This handler implements the CQRS write side by handling CommentReactionEvent and updating both
 * individual reaction entities and aggregated reaction counts in the database. Validation is
 * performed in the service layer before the event is emitted. WebSocket broadcasting is handled
 * centrally by {@link com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentReactionEventHandler implements EventHandler<CommentReactionEvent> {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(CommentReactionEvent event) {
        log.debug(
                "Persisting CommentReactionEvent for comment: {}, reaction: {}, added: {}",
                event.getCommentId(),
                event.getReactionType(),
                event.isAdded());

        Comment comment =
                commentRepository
                        .findById(event.getCommentId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Comment not found: " + event.getCommentId()));

        if (comment.getReactions() == null) {
            comment.setReactions(new Reactions());
        }

        ReactionType reactionType = ReactionType.valueOf(event.getReactionType());

        if (event.isAdded()) {
            // Check if this is a replacement operation
            if (event.getPreviousReactionType() != null) {
                // This is a replacement - delete old reaction first
                ReactionType previousReactionType =
                        ReactionType.valueOf(event.getPreviousReactionType());

                // Delete old reaction entity
                commentReactionRepository.deleteByCommentIdAndUserId(
                        event.getCommentId(), event.getUserId());

                // Flush to ensure delete is executed before insert
                commentReactionRepository.flush();

                // Decrement old reaction count
                comment.getReactions().decrementReaction(previousReactionType);

                log.info(
                        "Removed old reaction {} for comment: {}",
                        previousReactionType,
                        event.getCommentId());
            }

            // Add new individual reaction entity
            CommentReaction commentReaction =
                    CommentReaction.builder()
                            .id(UUID.randomUUID())
                            .comment(comment)
                            .user(comment.getUser()) // This will be set from userId
                            .reactionType(reactionType)
                            .timestamp(Instant.now())
                            .build();

            // Set user by ID to avoid loading full User entity
            commentReaction.setUser(
                    com.tomassirio.wanderer.commons.domain.User.builder()
                            .id(event.getUserId())
                            .build());

            commentReactionRepository.save(commentReaction);

            // Increment aggregated count
            comment.getReactions().incrementReaction(reactionType);

            log.info("Added reaction {} for comment: {}", reactionType, event.getCommentId());
        } else {
            // Remove individual reaction entity
            commentReactionRepository.deleteByCommentIdAndUserId(
                    event.getCommentId(), event.getUserId());

            // Decrement aggregated count
            comment.getReactions().decrementReaction(reactionType);

            log.info("Removed reaction {} for comment: {}", reactionType, event.getCommentId());
        }

        // No need to call save() on comment - entity is managed and will be flushed automatically
    }
}
