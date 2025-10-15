package com.tomassirio.wanderer.commons.mapper;

import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.dto.CommentDTO;
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
                    "java(comment.getUserId() != null ? comment.getUserId().toString() : null)")
    @Mapping(
            target = "tripId",
            expression =
                    "java(comment.getTrip() != null && comment.getTrip().getId() != null ? comment.getTrip().getId().toString() : null)")
    @Mapping(
            target = "parentCommentId",
            expression =
                    "java(comment.getParentComment() != null && comment.getParentComment().getId() != null ? comment.getParentComment().getId().toString() : null)")
    CommentDTO toDTO(Comment comment);

    @Mapping(
            target = "id",
            expression =
                    "java(commentDTO.id() != null ? java.util.UUID.fromString(commentDTO.id()) : null)")
    @Mapping(
            target = "userId",
            expression =
                    "java(commentDTO.userId() != null ? java.util.UUID.fromString(commentDTO.userId()) : null)")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}
