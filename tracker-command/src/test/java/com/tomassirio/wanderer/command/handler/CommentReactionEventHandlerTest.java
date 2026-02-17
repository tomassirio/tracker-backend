package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentReactionEventHandlerTest {

    @Mock private CommentRepository commentRepository;

    @InjectMocks private CommentReactionEventHandler handler;

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

        // Then - entity is managed, no need to verify save
        assertThat(comment.getReactions().getHeart()).isEqualTo(1);
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
}
