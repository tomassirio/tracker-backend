package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.CommentDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for querying comment data.
 *
 * @since 0.3.0
 */
public interface CommentService {

    /**
     * Retrieves a single comment by its unique identifier.
     *
     * @param commentId the UUID of the comment to retrieve
     * @return a {@link CommentDTO} containing the comment data
     * @throws jakarta.persistence.EntityNotFoundException if no comment exists with the given ID
     */
    CommentDTO getComment(UUID commentId);

    /**
     * Retrieves all top-level comments for a trip (with their replies).
     *
     * @param tripId the UUID of the trip
     * @return a list of {@link CommentDTO} objects representing top-level comments with their
     *     replies
     */
    List<CommentDTO> getCommentsForTrip(UUID tripId);
}
