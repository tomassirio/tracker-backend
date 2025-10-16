package com.tomassirio.wanderer.command.service.validator;

import java.util.UUID;
import java.util.function.Function;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Generic validator component responsible for checking ownership of entities. Ensures that users
 * can only perform operations on entities they own.
 */
@Component
public class OwnershipValidator {

    /**
     * Validates that the given user owns the entity. Throws AccessDeniedException if the user does
     * not own the entity.
     *
     * @param entity the entity to validate
     * @param userId the ID of the user attempting the operation
     * @param userIdExtractor function to extract the user ID from the entity
     * @param entityIdExtractor function to extract the entity ID for error messages
     * @param entityType the type of entity (e.g., "trip", "trip plan") for error messages
     * @param <T> the type of the entity
     * @throws AccessDeniedException if the user does not own the entity
     */
    public <T> void validateOwnership(
            T entity,
            UUID userId,
            Function<T, UUID> userIdExtractor,
            Function<T, UUID> entityIdExtractor,
            String entityType) {
        UUID ownerId = userIdExtractor.apply(entity);
        if (!ownerId.equals(userId)) {
            UUID entityId = entityIdExtractor.apply(entity);
            throw new AccessDeniedException(
                    "User "
                            + userId
                            + " does not have permission to modify "
                            + entityType
                            + " "
                            + entityId);
        }
    }
}
