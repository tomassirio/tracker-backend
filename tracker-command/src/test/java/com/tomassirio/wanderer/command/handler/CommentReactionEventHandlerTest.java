package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.repository.CommentReactionRepository;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.CommentReaction;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.domain.Reactions;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentReactionEventHandlerTest {

    @Mock private CommentRepository commentRepository;

    @Mock private CommentReactionRepository commentReactionRepository;

    @InjectMocks private CommentReactionEventHandler handler;

    @Test
    void handle_whenAddingReaction_shouldIncrementReactionCountAndSaveIndividualReaction() {
        // Given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment =
                Comment.builder().id(commentId).message("Test").reactions(new Reactions()).build();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .tripId(UUID.randomUUID())
                        .commentId(commentId)
                        .reactionType(ReactionType.HEART.name())
                        .userId(userId)
                        .added(true)
                        .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        handler.handle(event);

        // Then - verify aggregated count incremented
        assertThat(comment.getReactions().getHeart()).isEqualTo(1);

        // Verify individual reaction was saved
        ArgumentCaptor<CommentReaction> reactionCaptor =
                ArgumentCaptor.forClass(CommentReaction.class);
        verify(commentReactionRepository).save(reactionCaptor.capture());

        CommentReaction savedReaction = reactionCaptor.getValue();
        assertThat(savedReaction.getComment()).isEqualTo(comment);
        assertThat(savedReaction.getUser().getId()).isEqualTo(userId);
        assertThat(savedReaction.getReactionType()).isEqualTo(ReactionType.HEART);
    }

    @Test
    void handle_whenRemovingReaction_shouldDecrementReactionCountAndDeleteIndividualReaction() {
        // Given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Reactions reactions = new Reactions();
        reactions.setHeart(5);
        Comment comment = Comment.builder().id(commentId).reactions(reactions).build();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .commentId(commentId)
                        .reactionType(ReactionType.HEART.name())
                        .userId(userId)
                        .added(false)
                        .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        handler.handle(event);

        // Then - verify aggregated count decremented
        assertThat(comment.getReactions().getHeart()).isEqualTo(4);

        // Verify individual reaction was deleted
        verify(commentReactionRepository).deleteByCommentIdAndUserId(commentId, userId);
    }
}
