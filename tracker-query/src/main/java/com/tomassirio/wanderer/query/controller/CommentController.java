package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.CommentDTO;
import com.tomassirio.wanderer.query.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for comment query operations.
 *
 * @since 0.3.0
 */
@RestController
@RequestMapping(ApiConstants.API_V1)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment Queries", description = "Endpoints for retrieving comment information")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments/{id}")
    @Operation(
            summary = "Get comment by ID",
            description = "Retrieves a specific comment by its ID")
    public ResponseEntity<CommentDTO> getComment(@PathVariable UUID id) {
        log.info("Received request to retrieve comment: {}", id);

        CommentDTO comment = commentService.getComment(id);

        log.info("Successfully retrieved comment with ID: {}", comment.id());
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/trips/{tripId}/comments")
    @Operation(
            summary = "Get all comments for a trip",
            description = "Retrieves all top-level comments with their replies for a specific trip")
    public ResponseEntity<List<CommentDTO>> getCommentsForTrip(@PathVariable UUID tripId) {
        log.info("Received request to retrieve comments for trip: {}", tripId);

        List<CommentDTO> comments = commentService.getCommentsForTrip(tripId);

        log.info("Successfully retrieved {} comments for trip {}", comments.size(), tripId);
        return ResponseEntity.ok(comments);
    }
}
