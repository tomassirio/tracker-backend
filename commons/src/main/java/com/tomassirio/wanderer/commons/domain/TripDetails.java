package com.tomassirio.wanderer.commons.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDetails {

    @Column(name = "start_timestamp")
    private Instant startTimestamp;

    @Column(name = "end_timestamp")
    private Instant endTimestamp;

    @Type(JsonBinaryType.class)
    @Column(name = "start_location", columnDefinition = "jsonb")
    private GeoLocation startLocation;

    @Type(JsonBinaryType.class)
    @Column(name = "end_location", columnDefinition = "jsonb")
    private GeoLocation endLocation;
}
