package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.CommentCreationRequest;
import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.CommentService;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
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
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UUID createComment(UUID userId, UUID tripId, CommentCreationRequest request) {
        log.info("Creating comment for trip {} by user {}", tripId, userId);

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            log.info("This is a reply to comment {}", request.parentCommentId());
            parentComment =
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

        Comment comment =
                Comment.builder()
                        .user(user)
                        .trip(trip)
                        .parentComment(parentComment)
                        .message(request.message())
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();

        Comment savedComment = commentRepository.save(comment);

        String commentType = savedComment.isReply() ? "reply" : "comment";
        log.info("Successfully created {} with ID: {}", commentType, savedComment.getId());

        // Publish domain event - decoupled from WebSocket
        eventPublisher.publishEvent(
                CommentAddedEvent.builder()
                        .tripId(tripId)
                        .commentId(savedComment.getId())
                        .userId(userId)
                        .username(user.getUsername())
                        .message(request.message())
                        .parentCommentId(request.parentCommentId())
                        .build());

        return savedComment.getId();
    }

    @Override
    @Transactional
    public UUID addReactionToComment(UUID userId, UUID commentId, ReactionType reactionType) {
        log.info("Adding reaction {} to comment {} by user {}", reactionType, commentId, userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (comment.getReactions() == null) {
            comment.setReactions(new Reactions());
        }

        comment.getReactions().incrementReaction(reactionType);
        Comment savedComment = commentRepository.save(comment);

        log.info("Successfully added reaction {} to comment {}", reactionType, commentId);

        // Publish domain event - decoupled from WebSocket
        eventPublisher.publishEvent(
                CommentReactionEvent.builder()
                        .tripId(savedComment.getTrip().getId())
                        .commentId(commentId)
                        .reactionType(reactionType.name())
                        .userId(userId)
                        .added(true)
                        .build());

        return commentId;
    }

    @Override
    @Transactional
    public UUID removeReactionFromComment(UUID userId, UUID commentId, ReactionType reactionType) {
        log.info(
                "Removing reaction {} from comment {} by user {}", reactionType, commentId, userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (comment.getReactions() != null) {
            comment.getReactions().decrementReaction(reactionType);
            commentRepository.save(comment);
            log.info("Successfully removed reaction {} from comment {}", reactionType, commentId);

            // Publish domain event - decoupled from WebSocket
            eventPublisher.publishEvent(
                    CommentReactionEvent.builder()
                            .tripId(comment.getTrip().getId())
                            .commentId(commentId)
                            .reactionType(reactionType.name())
                            .userId(userId)
                            .added(false)
                            .build());
        } else {
            log.warn("No reactions found on comment {}", commentId);
        }

        return commentId;
    }
}
