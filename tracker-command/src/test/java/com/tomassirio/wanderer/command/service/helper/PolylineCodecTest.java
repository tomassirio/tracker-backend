package com.tomassirio.wanderer.command.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.model.LatLng;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class PolylineCodecTest {

    @Test
    void encode_whenNullPoints_shouldReturnNull() {
        // When & Then
        assertThat(PolylineCodec.encode(null)).isNull();
    }

    @Test
    void encode_whenEmptyPoints_shouldReturnNull() {
        // When & Then
        assertThat(PolylineCodec.encode(List.of())).isNull();
    }

    @Test
    void encode_whenValidPoints_shouldReturnNonEmptyString() {
        // Given
        List<LatLng> points = List.of(new LatLng(42.0, -8.0), new LatLng(43.0, -8.5));

        // When
        String result = PolylineCodec.encode(points);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void decode_whenNullInput_shouldReturnEmptyList() {
        // When & Then
        assertThat(PolylineCodec.decode(null)).isEmpty();
    }

    @Test
    void decode_whenEmptyInput_shouldReturnEmptyList() {
        // When & Then
        assertThat(PolylineCodec.decode("")).isEmpty();
    }

    @Test
    void decode_whenValidInput_shouldReturnPoints() {
        // Given — encode first to get a valid string
        List<LatLng> original = List.of(new LatLng(42.0, -8.0), new LatLng(43.0, -8.5));
        String encoded = PolylineCodec.encode(original);

        // When
        List<LatLng> decoded = PolylineCodec.decode(encoded);

        // Then
        assertThat(decoded).hasSize(2);
    }

    @Test
    void encodeAndDecode_shouldBeReversible() {
        // Given
        List<LatLng> originalPoints =
                List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2), new LatLng(43.0, -8.5));

        // When
        String encoded = PolylineCodec.encode(originalPoints);
        List<LatLng> decoded = PolylineCodec.decode(encoded);

        // Then — 1e-5 precision from the encoding algorithm
        assertThat(decoded).hasSameSizeAs(originalPoints);
        for (int i = 0; i < originalPoints.size(); i++) {
            assertThat(decoded.get(i).lat)
                    .isCloseTo(originalPoints.get(i).lat, Offset.offset(1e-5));
            assertThat(decoded.get(i).lng)
                    .isCloseTo(originalPoints.get(i).lng, Offset.offset(1e-5));
        }
    }

    @Test
    void encode_whenSinglePoint_shouldReturnValidString() {
        // Given
        List<LatLng> points = List.of(new LatLng(42.8805, -8.5457));

        // When
        String encoded = PolylineCodec.encode(points);

        // Then
        assertThat(encoded).isNotNull().isNotEmpty();

        List<LatLng> decoded = PolylineCodec.decode(encoded);
        assertThat(decoded).hasSize(1);
        assertThat(decoded.getFirst().lat).isCloseTo(42.8805, Offset.offset(1e-5));
        assertThat(decoded.getFirst().lng).isCloseTo(-8.5457, Offset.offset(1e-5));
    }
}
