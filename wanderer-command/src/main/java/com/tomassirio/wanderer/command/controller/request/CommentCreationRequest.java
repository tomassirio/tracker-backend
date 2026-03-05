package com.tomassirio.wanderer.command.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreationRequest(
        @Schema(description = "Comment message", example = "Great trip!")
                @NotBlank(message = "Message is required")
                @Size(max = 1000, message = "Message must not exceed 1000 characters")
                String message,
        @Schema(
                        description =
                                "Parent comment ID if this is a reply. Null for top-level comments.",
                        example = "123e4567-e89b-12d3-a456-426614174000")
                UUID parentCommentId) {}
