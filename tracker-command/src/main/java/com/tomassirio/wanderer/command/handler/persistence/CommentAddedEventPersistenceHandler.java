package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting comment creation events to the database.
 *
 * <p>This handler implements the CQRS write side by handling CommentAddedEvent and persisting new
 * comments to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class CommentAddedEventPersistenceHandler implements EventHandler<CommentAddedEvent> {

    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(CommentAddedEvent event) {
        log.debug("Persisting CommentAddedEvent for comment: {}", event.getCommentId());

        User user =
                userRepository
                        .findById(event.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Trip trip =
                tripRepository
                        .findById(event.getTripId())
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        Comment parentComment = null;
        if (event.getParentCommentId() != null) {
            parentComment =
                    commentRepository
                            .findById(event.getParentCommentId())
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Parent comment not found"));
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
