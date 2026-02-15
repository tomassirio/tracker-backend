package com.tomassirio.wanderer.command.service.impl;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USERNAME;
import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.CommentCreationRequest;
import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID PARENT_COMMENT_ID = UUID.randomUUID();
    private static final String COMMENT_MESSAGE = "Great trip!";
    private static final String REPLY_MESSAGE = "Thanks for sharing!";

    @Mock private CommentRepository commentRepository;

    @Mock private TripRepository tripRepository;

    @Mock private UserRepository userRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private CommentServiceImpl commentService;

    private User user;
    private Trip trip;
    private Comment topLevelComment;
    private Comment replyComment;

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).username(USERNAME).build();
        trip = Trip.builder().id(TRIP_ID).userId(USER_ID).name("Test Trip").build();

        topLevelComment =
                Comment.builder()
                        .id(COMMENT_ID)
                        .user(user)
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
                        .user(user)
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
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));

        // When
        UUID result = commentService.createComment(USER_ID, TRIP_ID, request);

        // Then
        assertThat(result).isNotNull();

        ArgumentCaptor<CommentAddedEvent> eventCaptor =
                ArgumentCaptor.forClass(CommentAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentAddedEvent event = eventCaptor.getValue();
        assertThat(event.getCommentId()).isEqualTo(result);
        assertThat(event.getUserId()).isEqualTo(USER_ID);
        assertThat(event.getTripId()).isEqualTo(TRIP_ID);
        assertThat(event.getParentCommentId()).isNull();
        assertThat(event.getMessage()).isEqualTo(COMMENT_MESSAGE);
        assertThat(event.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void createComment_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        CommentCreationRequest request = new CommentCreationRequest(COMMENT_MESSAGE, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(eventPublisher, never()).publishEvent(any(CommentAddedEvent.class));
    }

    // ============ CREATE REPLY TESTS ============

    @Test
    void createComment_whenValidReply_shouldCreateAndReturnReply() {
        // Given
        CommentCreationRequest request =
                new CommentCreationRequest(REPLY_MESSAGE, PARENT_COMMENT_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(PARENT_COMMENT_ID))
                .thenReturn(Optional.of(topLevelComment));

        // When
        UUID result = commentService.createComment(USER_ID, TRIP_ID, request);

        // Then
        assertThat(result).isNotNull();

        ArgumentCaptor<CommentAddedEvent> eventCaptor =
                ArgumentCaptor.forClass(CommentAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentAddedEvent event = eventCaptor.getValue();
        assertThat(event.getCommentId()).isEqualTo(result);
        assertThat(event.getParentCommentId()).isEqualTo(PARENT_COMMENT_ID);
    }

    @Test
    void createComment_whenParentCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        CommentCreationRequest request =
                new CommentCreationRequest(REPLY_MESSAGE, PARENT_COMMENT_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(PARENT_COMMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Parent comment not found");

        verify(eventPublisher, never()).publishEvent(any(CommentAddedEvent.class));
    }

    @Test
    void createComment_whenReplyingToReply_shouldThrowIllegalArgumentException() {
        // Given - Try to reply to a comment that is already a reply
        CommentCreationRequest request =
                new CommentCreationRequest("Reply to reply", replyComment.getId());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(replyComment.getId()))
                .thenReturn(Optional.of(replyComment));

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(USER_ID, TRIP_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create a reply to a reply. Maximum nesting depth is 1.");

        verify(eventPublisher, never()).publishEvent(any(CommentAddedEvent.class));
    }

    // ============ ADD REACTION TO COMMENT TESTS ============

    @Test
    void addReactionToComment_whenValidRequest_shouldPublishEventAndReturnCommentId() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));

        // When
        UUID result = commentService.addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(COMMENT_ID);

        ArgumentCaptor<CommentReactionEvent> eventCaptor =
                ArgumentCaptor.forClass(CommentReactionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentReactionEvent event = eventCaptor.getValue();
        assertThat(event.getCommentId()).isEqualTo(COMMENT_ID);
        assertThat(event.getTripId()).isEqualTo(TRIP_ID);
        assertThat(event.getReactionType()).isEqualTo(ReactionType.HEART.name());
        assertThat(event.getUserId()).isEqualTo(USER_ID);
        assertThat(event.isAdded()).isTrue();
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

        verify(eventPublisher, never()).publishEvent(any(CommentReactionEvent.class));
    }

    @Test
    void addReactionToComment_withAllReactionTypes_shouldPublishEventsCorrectly() {
        // Given
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));

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
            UUID result = commentService.addReactionToComment(USER_ID, COMMENT_ID, reactionType);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(COMMENT_ID);
        }

        // Verify events were published for each reaction type
        verify(eventPublisher, org.mockito.Mockito.times(5))
                .publishEvent(any(CommentReactionEvent.class));
    }

    // ============ REMOVE REACTION FROM COMMENT TESTS ============

    @Test
    void removeReactionFromComment_whenValidRequest_shouldPublishEventAndReturnCommentId() {
        // Given
        topLevelComment.getReactions().setHeart(5);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(topLevelComment));

        // When
        UUID result =
                commentService.removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(COMMENT_ID);

        ArgumentCaptor<CommentReactionEvent> eventCaptor =
                ArgumentCaptor.forClass(CommentReactionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentReactionEvent event = eventCaptor.getValue();
        assertThat(event.getCommentId()).isEqualTo(COMMENT_ID);
        assertThat(event.getReactionType()).isEqualTo(ReactionType.HEART.name());
        assertThat(event.isAdded()).isFalse();
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

        verify(eventPublisher, never()).publishEvent(any(CommentReactionEvent.class));
    }

    // ============ EDGE CASE TESTS ============

    @Test
    void addReactionToReply_shouldWorkSameAsTopLevelComment() {
        // Given - Test that replies can have reactions too
        when(commentRepository.findById(replyComment.getId()))
                .thenReturn(Optional.of(replyComment));

        // When
        UUID result =
                commentService.addReactionToComment(
                        USER_ID, replyComment.getId(), ReactionType.LAUGH);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(replyComment.getId());

        ArgumentCaptor<CommentReactionEvent> eventCaptor =
                ArgumentCaptor.forClass(CommentReactionEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentReactionEvent event = eventCaptor.getValue();
        assertThat(event.getCommentId()).isEqualTo(replyComment.getId());
        assertThat(event.getReactionType()).isEqualTo(ReactionType.LAUGH.name());
        assertThat(event.isAdded()).isTrue();
    }
}
