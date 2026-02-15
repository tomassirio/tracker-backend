package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentReactionEventPersistenceHandlerTest {

    @Mock private CommentRepository commentRepository;

    @InjectMocks private CommentReactionEventPersistenceHandler handler;

    @Test
    void handle_whenAddingReaction_shouldIncrementReactionCount() {
        // Given
        UUID commentId = UUID.randomUUID();
        Comment comment =
                Comment.builder().id(commentId).message("Test").reactions(new Reactions()).build();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .tripId(UUID.randomUUID())
                        .commentId(commentId)
                        .reactionType(ReactionType.HEART.name())
                        .userId(UUID.randomUUID())
                        .added(true)
                        .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getReactions().getHeart()).isEqualTo(1);
    }

    @Test
    void handle_whenRemovingReaction_shouldDecrementReactionCount() {
        // Given
        UUID commentId = UUID.randomUUID();
        Reactions reactions = new Reactions();
        reactions.setHeart(5);
        Comment comment = Comment.builder().id(commentId).reactions(reactions).build();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .commentId(commentId)
                        .reactionType(ReactionType.HEART.name())
                        .added(false)
                        .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        handler.handle(event);

        // Then
        assertThat(comment.getReactions().getHeart()).isEqualTo(4);
    }

    @Test
    void handle_whenCommentNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID commentId = UUID.randomUUID();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .commentId(commentId)
                        .reactionType(ReactionType.HEART.name())
                        .added(true)
                        .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment not found");
        verify(commentRepository, never()).save(any());
    }
}
