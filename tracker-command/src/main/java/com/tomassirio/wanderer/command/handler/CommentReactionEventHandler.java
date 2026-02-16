package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting comment reaction events to the database.
 *
 * <p>This handler implements the CQRS write side by handling CommentReactionEvent and updating
 * reaction counts in the database. Validation is performed in the service layer before the event is
 * emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentReactionEventHandler implements EventHandler<CommentReactionEvent> {

    private final CommentRepository commentRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(CommentReactionEvent event) {
        log.debug(
                "Persisting CommentReactionEvent for comment: {}, reaction: {}, added: {}",
                event.getCommentId(),
                event.getReactionType(),
                event.isAdded());

        // Comment is validated in the service layer before event emission
        Comment comment = commentRepository.getReferenceById(event.getCommentId());

        if (comment.getReactions() == null) {
            comment.setReactions(new Reactions());
        }

        ReactionType reactionType = ReactionType.valueOf(event.getReactionType());

        if (event.isAdded()) {
            comment.getReactions().incrementReaction(reactionType);
        } else {
            comment.getReactions().decrementReaction(reactionType);
        }

        commentRepository.save(comment);
        log.info(
                "Reaction {} {} for comment: {}",
                event.getReactionType(),
                event.isAdded() ? "added" : "removed",
                event.getCommentId());
    }
}
