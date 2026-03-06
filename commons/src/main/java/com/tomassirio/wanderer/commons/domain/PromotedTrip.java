package com.tomassirio.wanderer.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promoted_trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotedTrip {

    @Id private UUID id;

    @NotNull
    @Column(name = "trip_id", nullable = false, unique = true)
    private UUID tripId;

    @Column(name = "donation_link")
    private String donationLink;

    @NotNull
    @Column(name = "promoted_by", nullable = false)
    private UUID promotedBy;

    @NotNull
    @Column(name = "promoted_at", nullable = false)
    private Instant promotedAt;

    @NotNull
    @Column(name = "is_pre_announced", nullable = false)
    @Builder.Default
    private boolean preAnnounced = false;

    @Column(name = "countdown_start_date")
    private Instant countdownStartDate;
}
