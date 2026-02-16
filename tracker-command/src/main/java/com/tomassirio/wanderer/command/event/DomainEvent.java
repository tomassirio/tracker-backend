package com.tomassirio.wanderer.command.event;

/**
 * Marker interface for all domain events in the system.
 *
 * <p>All events that represent state changes in the domain model should implement this interface.
 * This provides a common type for event handling infrastructure and enables type-safe event
 * processing.
 */
public interface DomainEvent {}
