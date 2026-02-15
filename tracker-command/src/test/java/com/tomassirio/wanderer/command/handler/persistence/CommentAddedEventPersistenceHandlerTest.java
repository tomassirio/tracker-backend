package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAddedEventPersistenceHandlerTest {

    @Mock private CommentRepository commentRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CommentAddedEventPersistenceHandler handler;

    @Test
    void handle_whenValidTopLevelComment_shouldPersistComment() {
        // Given
        UUID commentId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        User user = User.builder().id(userId).username("testuser").build();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();

        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .commentId(commentId)
                        .tripId(tripId)
                        .userId(userId)
                        .username("testuser")
                        .message("Great trip!")
                        .parentCommentId(null)
                        .timestamp(timestamp)
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getId()).isEqualTo(commentId);
        assertThat(savedComment.getUser()).isEqualTo(user);
        assertThat(savedComment.getTrip()).isEqualTo(trip);
        assertThat(savedComment.getMessage()).isEqualTo("Great trip!");
        assertThat(savedComment.getParentComment()).isNull();
        assertThat(savedComment.getReactions()).isNotNull();
        assertThat(savedComment.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void handle_whenValidReply_shouldPersistCommentWithParent() {
        // Given
        UUID commentId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        User user = User.builder().id(userId).username("testuser").build();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();
        Comment parentComment =
                Comment.builder()
                        .id(parentCommentId)
                        .user(user)
                        .trip(trip)
                        .message("Parent comment")
                        .reactions(new Reactions())
                        .build();

        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .commentId(commentId)
                        .tripId(tripId)
                        .userId(userId)
                        .username("testuser")
                        .message("This is a reply")
                        .parentCommentId(parentCommentId)
                        .timestamp(timestamp)
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getParentComment()).isEqualTo(parentComment);
    }

    @Test
    void handle_whenUserNotFound_shouldThrowEntityNotFoundException() {
        // Given
        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .commentId(UUID.randomUUID())
                        .tripId(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .username("testuser")
                        .message("Great trip!")
                        .timestamp(Instant.now())
                        .build();

        when(userRepository.findById(event.getUserId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void handle_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testuser").build();

        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .commentId(UUID.randomUUID())
                        .tripId(UUID.randomUUID())
                        .userId(userId)
                        .username("testuser")
                        .message("Great trip!")
                        .timestamp(Instant.now())
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tripRepository.findById(event.getTripId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void handle_whenParentCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();

        User user = User.builder().id(userId).username("testuser").build();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();

        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .commentId(UUID.randomUUID())
                        .tripId(tripId)
                        .userId(userId)
                        .username("testuser")
                        .message("This is a reply")
                        .parentCommentId(parentCommentId)
                        .timestamp(Instant.now())
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Parent comment not found");

        verify(commentRepository, never()).save(any());
    }
}
