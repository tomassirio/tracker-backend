package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.CommentCreationRequest;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.CommentService;
import com.tomassirio.wanderer.command.websocket.CommentAddedPayload;
import com.tomassirio.wanderer.command.websocket.CommentReactionPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.dto.CommentDTO;
import com.tomassirio.wanderer.commons.mapper.CommentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper = CommentMapper.INSTANCE;
    private final WebSocketEventService webSocketEventService;

    @Override
    @Transactional
    public CommentDTO createComment(UUID userId, UUID tripId, CommentCreationRequest request) {
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

        CommentDTO result = commentMapper.toDTO(savedComment);

        // Broadcast comment added via WebSocket
        CommentAddedPayload payload =
                CommentAddedPayload.create(
                        tripId,
                        savedComment.getId(),
                        userId,
                        user.getUsername(),
                        request.message(),
                        request.parentCommentId());

        webSocketEventService.broadcastCommentAdded(payload);

        return result;
    }

    @Override
    @Transactional
    public CommentDTO addReactionToComment(UUID userId, UUID commentId, ReactionType reactionType) {
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

        CommentDTO result = commentMapper.toDTO(savedComment);

        // Broadcast reaction added via WebSocket
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(savedComment.getTrip().getId())
                        .commentId(commentId)
                        .reactionType(reactionType.name())
                        .userId(userId)
                        .build();

        webSocketEventService.broadcastCommentReactionAdded(payload);

        return result;
    }

    @Override
    @Transactional
    public CommentDTO removeReactionFromComment(
            UUID userId, UUID commentId, ReactionType reactionType) {
        log.info(
                "Removing reaction {} from comment {} by user {}", reactionType, commentId, userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (comment.getReactions() != null) {
            comment.getReactions().decrementReaction(reactionType);
            Comment savedComment = commentRepository.save(comment);
            log.info("Successfully removed reaction {} from comment {}", reactionType, commentId);

            // Broadcast reaction removed via WebSocket
            CommentReactionPayload payload =
                    CommentReactionPayload.builder()
                            .tripId(savedComment.getTrip().getId())
                            .commentId(commentId)
                            .reactionType(reactionType.name())
                            .userId(userId)
                            .build();

            webSocketEventService.broadcastCommentReactionRemoved(payload);

            return commentMapper.toDTO(savedComment);
        }

        log.warn("No reactions found on comment {}", commentId);
        return commentMapper.toDTO(comment);
    }
}
