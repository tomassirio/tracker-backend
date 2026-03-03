package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.CommentReaction;
import com.tomassirio.wanderer.commons.dto.CommentDTO;
import com.tomassirio.wanderer.commons.dto.CommentReactionDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(
            target = "id",
            expression = "java(comment.getId() != null ? comment.getId().toString() : null)")
    @Mapping(
            target = "userId",
            expression =
                    "java(comment.getUser() != null && comment.getUser().getId() != null ? comment.getUser().getId().toString() : null)")
    @Mapping(
            target = "username",
            expression = "java(comment.getUser() != null ? comment.getUser().getUsername() : null)")
    @Mapping(
            target = "tripId",
            expression =
                    "java(comment.getTrip() != null && comment.getTrip().getId() != null ? comment.getTrip().getId().toString() : null)")
    @Mapping(
            target = "parentCommentId",
            expression =
                    "java(comment.getParentComment() != null && comment.getParentComment().getId() != null ? comment.getParentComment().getId().toString() : null)")
    @Mapping(
            target = "individualReactions",
            expression = "java(mapIndividualReactions(comment.getCommentReactions()))")
    CommentDTO toDTO(Comment comment);

    @Mapping(
            target = "id",
            expression =
                    "java(commentDTO.id() != null ? java.util.UUID.fromString(commentDTO.id()) : null)")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "commentReactions", ignore = true)
    Comment toEntity(CommentDTO commentDTO);

    /**
     * Maps a list of CommentReaction entities to CommentReactionDTO objects.
     *
     * @param commentReactions the list of comment reactions
     * @return the list of CommentReactionDTO objects
     */
    default List<CommentReactionDTO> mapIndividualReactions(
            List<CommentReaction> commentReactions) {
        if (commentReactions == null) {
            return List.of();
        }
        return commentReactions.stream()
                .map(
                        reaction ->
                                new CommentReactionDTO(
                                        reaction.getUser() != null
                                                        && reaction.getUser().getId() != null
                                                ? reaction.getUser().getId().toString()
                                                : null,
                                        reaction.getUser() != null
                                                ? reaction.getUser().getUsername()
                                                : null,
                                        reaction.getReactionType() != null
                                                ? reaction.getReactionType().name()
                                                : null,
                                        reaction.getTimestamp()))
                .toList();
    }
}
