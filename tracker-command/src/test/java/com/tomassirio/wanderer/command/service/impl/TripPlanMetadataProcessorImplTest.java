package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripPlanMetadataProcessorImplTest {

    private static final String DISTANCE_PER_DAY_KEY = "distancePerDay";

    private TripPlanMetadataProcessorImpl processor;

    @BeforeEach
    void setUp() {
        processor = new TripPlanMetadataProcessorImpl();
    }

    @Test
    void processMetadata_whenSimplePlanWithNullMetadata_shouldReturnEmptyMap() {
        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.SIMPLE, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void processMetadata_whenSimplePlanWithEmptyMetadata_shouldReturnEmptyMap() {
        // Given
        Map<String, Object> metadata = new HashMap<>();

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.SIMPLE, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void processMetadata_whenSimplePlanWithMetadata_shouldReturnCopyOfMetadata() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.SIMPLE, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get("key1")).isEqualTo("value1");
        assertThat(result.get("key2")).isEqualTo(123);
        assertThat(result).isNotSameAs(metadata); // Should be a copy
    }

    @Test
    void processMetadata_whenSimplePlanWithArbitraryData_shouldNotValidate() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, -100); // Negative value, but SIMPLE doesn't validate

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.SIMPLE, metadata);

        // Then - Should not throw, just returns a copy
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(-100);
    }

    @Test
    void processMetadata_whenMultiDayWithNullMetadata_shouldReturnEmptyMap() {
        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void processMetadata_whenMultiDayWithEmptyMetadata_shouldReturnEmptyMap() {
        // Given
        Map<String, Object> metadata = new HashMap<>();

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void processMetadata_whenMultiDayWithValidDistancePerDay_shouldProcessSuccessfully() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 50.5);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(50.5);
    }

    @Test
    void processMetadata_whenMultiDayWithIntegerDistancePerDay_shouldConvertToDouble() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 100);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(100.0);
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isInstanceOf(Double.class);
    }

    @Test
    void processMetadata_whenMultiDayWithStringDistancePerDay_shouldConvertToDouble() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "75.5");

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(75.5);
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isInstanceOf(Double.class);
    }

    @Test
    void processMetadata_whenMultiDayWithFloatDistancePerDay_shouldConvertToDouble() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 42.3f);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isInstanceOf(Double.class);
    }

    @Test
    void processMetadata_whenMultiDayWithLongDistancePerDay_shouldConvertToDouble() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 200L);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(200.0);
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isInstanceOf(Double.class);
    }

    @Test
    void processMetadata_whenMultiDayWithNegativeDistancePerDay_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, -50.0);

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance per day must be positive")
                .hasMessageContaining("-50.0");
    }

    @Test
    void processMetadata_whenMultiDayWithZeroDistancePerDay_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 0.0);

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance per day must be positive")
                .hasMessageContaining("0.0");
    }

    @Test
    void processMetadata_whenMultiDayWithInvalidStringDistancePerDay_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "invalid");

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid distance per day value")
                .hasMessageContaining("invalid");
    }

    @Test
    void processMetadata_whenMultiDayWithNullDistancePerDay_shouldReturnAsIs() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, null);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKey(DISTANCE_PER_DAY_KEY);
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isNull();
    }

    @Test
    void processMetadata_whenMultiDayWithOtherMetadata_shouldPreserveOtherFields() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 100.0);
        metadata.put("customField1", "value1");
        metadata.put("customField2", 42);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(100.0);
        assertThat(result.get("customField1")).isEqualTo("value1");
        assertThat(result.get("customField2")).isEqualTo(42);
    }

    @Test
    void processMetadata_whenMultiDayWithoutDistancePerDay_shouldReturnAsIs() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("otherField", "value");

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get("otherField")).isEqualTo("value");
    }

    @Test
    void processMetadata_whenMultiDayWithVeryLargeDistance_shouldProcess() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 999999.99);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(999999.99);
    }

    @Test
    void processMetadata_whenMultiDayWithVerySmallPositiveDistance_shouldProcess() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 0.001);

        // When
        Map<String, Object> result = processor.processMetadata(TripPlanType.MULTI_DAY, metadata);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get(DISTANCE_PER_DAY_KEY)).isEqualTo(0.001);
    }

    // APPLY METADATA TESTS

    @Test
    void applyMetadata_whenTripPlanIsNull_shouldNotThrow() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // When & Then - Should not throw
        processor.applyMetadata(null, metadata);
    }

    @Test
    void applyMetadata_whenSimplePlanWithMetadata_shouldSetMetadata() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.SIMPLE).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);

        // When
        processor.applyMetadata(tripPlan, metadata);

        // Then
        assertThat(tripPlan.getMetadata()).isNotNull();
        assertThat(tripPlan.getMetadata()).hasSize(2);
        assertThat(tripPlan.getMetadata().get("key1")).isEqualTo("value1");
        assertThat(tripPlan.getMetadata().get("key2")).isEqualTo(123);
    }

    @Test
    void applyMetadata_whenMultiDayPlanWithValidDistance_shouldSetMetadata() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.MULTI_DAY).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 75.5);

        // When
        processor.applyMetadata(tripPlan, metadata);

        // Then
        assertThat(tripPlan.getMetadata()).isNotNull();
        assertThat(tripPlan.getMetadata().get(DISTANCE_PER_DAY_KEY)).isEqualTo(75.5);
    }

    @Test
    void applyMetadata_whenMultiDayPlanWithInvalidDistance_shouldThrowException() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.MULTI_DAY).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, -50.0);

        // When & Then
        assertThatThrownBy(() -> processor.applyMetadata(tripPlan, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Distance per day must be positive");
    }

    @Test
    void applyMetadata_whenNullMetadata_shouldSetEmptyMap() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.SIMPLE).build();

        // When
        processor.applyMetadata(tripPlan, null);

        // Then
        assertThat(tripPlan.getMetadata()).isNotNull();
        assertThat(tripPlan.getMetadata()).isEmpty();
    }

    @Test
    void applyMetadata_whenMultiDayWithMixedValidAndInvalidData_shouldProcessCorrectly() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.MULTI_DAY).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "100");
        metadata.put("customData", "some value");

        // When
        processor.applyMetadata(tripPlan, metadata);

        // Then
        assertThat(tripPlan.getMetadata()).isNotNull();
        assertThat(tripPlan.getMetadata().get(DISTANCE_PER_DAY_KEY)).isEqualTo(100.0);
        assertThat(tripPlan.getMetadata().get("customData")).isEqualTo("some value");
    }

    @Test
    void applyMetadata_whenCalledMultipleTimes_shouldReplaceMetadata() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.SIMPLE).build();
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("key1", "value1");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("key2", "value2");

        // When
        processor.applyMetadata(tripPlan, metadata1);
        processor.applyMetadata(tripPlan, metadata2);

        // Then
        assertThat(tripPlan.getMetadata()).isNotNull();
        assertThat(tripPlan.getMetadata()).hasSize(1);
        assertThat(tripPlan.getMetadata().get("key2")).isEqualTo("value2");
        assertThat(tripPlan.getMetadata()).doesNotContainKey("key1");
    }

    @Test
    void applyMetadata_shouldNotModifyOriginalMetadataMap() {
        // Given
        TripPlan tripPlan = TripPlan.builder().planType(TripPlanType.MULTI_DAY).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, 50);
        Map<String, Object> originalMetadata = new HashMap<>(metadata);

        // When
        processor.applyMetadata(tripPlan, metadata);

        // Then
        assertThat(metadata).isEqualTo(originalMetadata);
        assertThat(tripPlan.getMetadata()).isNotSameAs(metadata);
    }

    // EDGE CASE TESTS

    @Test
    void processMetadata_whenMultiDayWithEmptyStringDistance_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "");

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid distance per day value");
    }

    @Test
    void processMetadata_whenMultiDayWithWhitespaceDistance_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "   ");

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid distance per day value");
    }

    @Test
    void processMetadata_whenMultiDayWithSpecialCharactersDistance_shouldThrowException() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DISTANCE_PER_DAY_KEY, "abc123");

        // When & Then
        assertThatThrownBy(() -> processor.processMetadata(TripPlanType.MULTI_DAY, metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid distance per day value");
    }

    @Test
    void processMetadata_shouldReturnNewMapInstance() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // When
        Map<String, Object> result1 = processor.processMetadata(TripPlanType.SIMPLE, metadata);
        Map<String, Object> result2 = processor.processMetadata(TripPlanType.SIMPLE, metadata);

        // Then
        assertThat(result1).isNotSameAs(metadata);
        assertThat(result2).isNotSameAs(metadata);
        assertThat(result1).isNotSameAs(result2);
    }
}
