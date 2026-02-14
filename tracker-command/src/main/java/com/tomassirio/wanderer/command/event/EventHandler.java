package com.tomassirio.wanderer.command.event;

/**
 * Interface for handling domain events.
 *
 * <p>Implementations of this interface are responsible for processing specific types of domain
 * events. Event handlers should be stateless and idempotent where possible.
 *
 * @param <T> the type of domain event this handler processes
 */
public interface EventHandler<T extends DomainEvent> {

    /**
     * Handles the given domain event.
     *
     * @param event the domain event to process
     */
    void handle(T event);
}
