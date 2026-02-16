package com.tomassirio.wanderer.command.controller.request;

import com.tomassirio.wanderer.commons.domain.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(
        @Schema(
                        description = "Type of reaction",
                        example = "HEART",
                        allowableValues = {"HEART", "SMILEY", "SAD", "LAUGH", "ANGER"})
                @NotNull(message = "Reaction type is required")
                ReactionType reactionType) {}
