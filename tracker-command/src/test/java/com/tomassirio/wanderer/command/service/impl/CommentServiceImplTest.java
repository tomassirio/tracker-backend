package com.tomassirio.wanderer.command.service.impl;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.CommentCreationRequest;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.dto.CommentDTO;
import com.tomassirio.wanderer.commons.mapper.CommentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID PARENT_COMMENT_ID = UUID.randomUUID();
    private static final String COMMENT_MESSAGE = "Great trip!";
    private static final String REPLY_MESSAGE = "Thanks for sharing!";

    @Mock private CommentRepository commentRepository;

    @Mock private TripRepository tripRepository;

    @Spy private CommentMapper commentMapper = CommentMapper.INSTANCE;

    @InjectMocks private CommentServiceImpl commentService;

    private Trip trip;
    private Comment topLevelComment;
    private Comment replyComment;

    @BeforeEach
    void setUp() {
        trip = Trip.builder().id(TRIP_ID).userId(USER_ID).name("Test Trip").build();

        topLevelComment =
                Comment.builder()
                        .id(COMMENT_ID)
                        .userId(USER_ID)
                        .trip(trip)
                        .parentComment(null)
                        .message(COMMENT_MESSAGE)
                        .reactions(new Reactions())
                        .replies(new ArrayList<>())
                        .timestamp(Instant.now())
                        .build();

        replyComment =
                Comment.builder()
                        .id(UUID.randomUUID())
                        .userId(USER_ID)
                        .trip(trip)
                        .parentComment(topLevelComment)
                        .message(REPLY_MESSAGE)
                        .reactions(new Reactions())
                        .replies(new ArrayList<>())
                        .timestamp(Instant.now())
                        .build();
    }

    // ============ CREATE TOP-LEVEL COMMENT TESTS ============

    @Test
    void createComment_whenValidTopLevelComment_shouldCreateAndReturnComment() {
        // Given
        CommentCreationRequest request = new CommentCreationRequest(COMMENT_MESSAGE, null);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When
        CommentDTO result = commentService.createComment(USER_ID, TRIP_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(COMMENT_ID.toString());
        assertThat(result.userId()).isEqualTo(USER_ID.toString());
        assertThat(result.tripId()).isEqualTo(TRIP_ID.toString());
        assertThat(result.parentCommentId()).isNull();
        assertThat(result.message()).isEqualTo(COMMENT_MESSAGE);
        assertThat(result.reactions()).isNotNull();

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getUserId()).isEqualTo(USER_ID);
        assertThat(savedComment.getTrip()).isEqualTo(trip);
        assertThat(savedComment.getParentComment()).isNull();
        assertThat(savedComment.getMessage()).isEqualTo(COMMENT_MESSAGE);
        assertThat(savedComment.getReactions()).isNotNull();
    }

    @Test
    void createComment_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        CommentCreationRequest request = new CommentCreationRequest(COMMENT_MESSAGE, null);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ============ CREATE REPLY TESTS ============

    @Test
    void createComment_whenValidReply_shouldCreateAndReturnReply() {
        // Given
        CommentCreationRequest request =
                new CommentCreationRequest(REPLY_MESSAGE, PARENT_COMMENT_ID);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(PARENT_COMMENT_ID))
                .thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        // When
        CommentDTO result = commentService.createComment(USER_ID, TRIP_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(USER_ID.toString());
        assertThat(result.tripId()).isEqualTo(TRIP_ID.toString());
        assertThat(result.parentCommentId()).isNotNull();
        assertThat(result.message()).isEqualTo(REPLY_MESSAGE);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getParentComment()).isEqualTo(topLevelComment);
    }

    @Test
    void createComment_whenParentCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        CommentCreationRequest request =
                new CommentCreationRequest(REPLY_MESSAGE, PARENT_COMMENT_ID);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(PARENT_COMMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Parent comment not found");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenReplyingToReply_shouldThrowIllegalArgumentException() {
        // Given - Try to reply to a comment that is already a reply
        CommentCreationRequest request =
                new CommentCreationRequest("Reply to reply", replyComment.getId());
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(replyComment.getId()))
                .thenReturn(Optional.of(replyComment));

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create a reply to a reply. Maximum nesting depth is 1.");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ============ ADD REACTION TO COMMENT TESTS ============

    @Test
    void addReactionToComment_whenValidRequest_shouldIncrementReactionAndReturnComment() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When
        CommentDTO result =
                commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(COMMENT_ID.toString());
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(1);

        verify(commentRepository).save(topLevelComment);
    }

    @Test
    void addReactionToComment_whenReactionsIsNull_shouldCreateReactionsAndIncrement() {
        // Given
        topLevelComment.setReactions(null);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When
        CommentDTO result =
                commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.SMILEY);

        // Then
        assertThat(result).isNotNull();
        assertThat(topLevelComment.getReactions()).isNotNull();
        assertThat(topLevelComment.getReactions().getSmiley()).isEqualTo(1);

        verify(commentRepository).save(topLevelComment);
    }

    @Test
    void addReactionToComment_whenCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                commentService.addReactionToComment(
                                        USER_ID, COMMENT_ID, ReactionType.HEART))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment not found");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addReactionToComment_withAllReactionTypes_shouldWorkCorrectly() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // Test all reaction types
        ReactionType[] reactionTypes =
                new ReactionType[] {
                    ReactionType.HEART,
                    ReactionType.SMILEY,
                    ReactionType.SAD,
                    ReactionType.LAUGH,
                    ReactionType.ANGER
                };

        for (ReactionType reactionType : reactionTypes) {
            // When
            CommentDTO result =
                    commentService.addReactionToComment(USER_ID, COMMENT_ID, reactionType);

            // Then
            assertThat(result).isNotNull();
        }

        // Verify all reactions were incremented
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(1);
        assertThat(topLevelComment.getReactions().getSmiley()).isEqualTo(1);
        assertThat(topLevelComment.getReactions().getSad()).isEqualTo(1);
        assertThat(topLevelComment.getReactions().getLaugh()).isEqualTo(1);
        assertThat(topLevelComment.getReactions().getAnger()).isEqualTo(1);
    }

    // ============ REMOVE REACTION FROM COMMENT TESTS ============

    @Test
    void removeReactionFromComment_whenValidRequest_shouldDecrementReactionAndReturnComment() {
        // Given
        topLevelComment.getReactions().setHeart(5);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When
        CommentDTO result =
                commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(COMMENT_ID.toString());
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(4);

        verify(commentRepository).save(topLevelComment);
    }

    @Test
    void removeReactionFromComment_whenReactionIsZero_shouldNotGoBelowZero() {
        // Given
        topLevelComment.getReactions().setHeart(0);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When
        CommentDTO result =
                commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(0);

        verify(commentRepository).save(topLevelComment);
    }

    @Test
    void removeReactionFromComment_whenReactionsIsNull_shouldReturnCommentWithoutSaving() {
        // Given
        topLevelComment.setReactions(null);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));

        // When
        CommentDTO result =
                commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(COMMENT_ID.toString());

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void removeReactionFromComment_whenCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                commentService.removeReactionFromComment(
                                        USER_ID, COMMENT_ID, ReactionType.HEART))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment not found");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ============ EDGE CASE TESTS ============

    @Test
    void reactions_whenMultipleIncrementsAndDecrements_shouldTrackCorrectly() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(topLevelComment);

        // When - Add reactions multiple times
        commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);
        commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);
        commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(3);

        // When - Remove reactions
        commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(2);

        // When - Try to remove below zero
        commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);
        commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);
        commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then - Should stay at 0
        assertThat(topLevelComment.getReactions().getHeart()).isEqualTo(0);
    }

    @Test
    void addReactionToReply_shouldWorkSameAsTopLevelComment() {
        // Given - Test that replies can have reactions too
        when(commentRepository.findById(replyComment.getId()))
                .thenReturn(Optional.of(replyComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        // When
        CommentDTO result =
                commentService.addReactionToComment(
                        USER_ID, replyComment.getId(), ReactionType.LAUGH);

        // Then
        assertThat(result).isNotNull();
        assertThat(replyComment.getReactions().getLaugh()).isEqualTo(1);

        verify(commentRepository).save(replyComment);
    }
}
