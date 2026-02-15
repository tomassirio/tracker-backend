package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting comment creation events to the database.
 *
 * <p>This handler implements the CQRS write side by handling CommentAddedEvent and persisting new
 * comments to the database. Validation is performed in the service layer before the event is
 * emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAddedEventHandler implements EventHandler<CommentAddedEvent> {

    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(CommentAddedEvent event) {
        log.debug("Persisting CommentAddedEvent for comment: {}", event.getCommentId());

        // Entities are validated in the service layer before event emission
        User user = userRepository.getReferenceById(event.getUserId());
        Trip trip = tripRepository.getReferenceById(event.getTripId());

        Comment parentComment = null;
        if (event.getParentCommentId() != null) {
            parentComment = commentRepository.getReferenceById(event.getParentCommentId());
        }

        Comment comment =
                Comment.builder()
                        .id(event.getCommentId())
                        .user(user)
                        .trip(trip)
                        .parentComment(parentComment)
                        .message(event.getMessage())
                        .reactions(new Reactions())
                        .timestamp(event.getTimestamp())
                        .build();

        commentRepository.save(comment);
        log.info("Comment created and persisted: {}", event.getCommentId());
    }
}
