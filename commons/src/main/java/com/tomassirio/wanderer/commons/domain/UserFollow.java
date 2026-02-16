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
@Table(name = "user_follows")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollow {

    @Id private UUID id;

    @NotNull
    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @NotNull
    @Column(name = "followed_id", nullable = false)
    private UUID followedId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
