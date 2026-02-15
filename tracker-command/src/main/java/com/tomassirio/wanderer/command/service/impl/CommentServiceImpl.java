package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.controller.request.CommentCreationRequest;
import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.CommentService;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID createComment(UUID userId, UUID tripId, CommentCreationRequest request) {
        log.info("Creating comment for trip {} by user {}", tripId, userId);

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Validate trip exists
        tripRepository
                .findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        // Validate parent comment if this is a reply
        if (request.parentCommentId() != null) {
            log.info("This is a reply to comment {}", request.parentCommentId());
            Comment parentComment =
                    commentRepository
                            .findById(request.parentCommentId())
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Parent comment not found"));

            // Enforce max depth of 1: cannot reply to a reply
            if (parentComment.isReply()) {
                throw new IllegalArgumentException(
                        "Cannot create a reply to a reply. Maximum nesting depth is 1.");
            }
        }

        // Pre-generate ID and timestamp
        UUID commentId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                CommentAddedEvent.builder()
                        .commentId(commentId)
                        .tripId(tripId)
                        .userId(userId)
                        .username(user.getUsername())
                        .message(request.message())
                        .parentCommentId(request.parentCommentId())
                        .timestamp(timestamp)
                        .build());

        String commentType = request.parentCommentId() != null ? "reply" : "comment";
        log.info("Successfully created {} with ID: {}", commentType, commentId);

        return commentId;
    }

    @Override
    public UUID addReactionToComment(UUID userId, UUID commentId, ReactionType reactionType) {
        log.info("Adding reaction {} to comment {} by user {}", reactionType, commentId, userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Publish event - persistence handler will update DB
        eventPublisher.publishEvent(
                CommentReactionEvent.builder()
                        .tripId(comment.getTrip().getId())
                        .commentId(commentId)
                        .reactionType(reactionType.name())
                        .userId(userId)
                        .added(true)
                        .build());

        log.info("Successfully added reaction {} to comment {}", reactionType, commentId);

        return commentId;
    }

    @Override
    public UUID removeReactionFromComment(UUID userId, UUID commentId, ReactionType reactionType) {
        log.info(
                "Removing reaction {} from comment {} by user {}", reactionType, commentId, userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Publish event - persistence handler will update DB
        eventPublisher.publishEvent(
                CommentReactionEvent.builder()
                        .tripId(comment.getTrip().getId())
                        .commentId(commentId)
                        .reactionType(reactionType.name())
                        .userId(userId)
                        .added(false)
                        .build());

        log.info("Successfully removed reaction {} from comment {}", reactionType, commentId);

        return commentId;
    }
}
