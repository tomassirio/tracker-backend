package com.tomassirio.wanderer.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the currently active (IN_PROGRESS) trip for a user.
 *
 * <p>This entity enforces the business rule that a user can have only one trip in progress at a
 * time. The table maintains a one-to-one relationship between users and their active trips.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@Entity
@Table(name = "active_trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveTrip {

    @Id
    @Column(name = "user_id")
    @NotNull
    private UUID userId;

    @Column(name = "trip_id", nullable = false)
    @NotNull
    private UUID tripId;
}
