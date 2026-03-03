package com.tomassirio.wanderer.command.service.helper;

import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Stateless utility for encoding and decoding Google Encoded Polylines.
 *
 * <p>Uses the standard <a
 * href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Google
 * Encoded Polyline Algorithm</a> with 1e-5 precision (5 decimal places).
 *
 * @since 0.8.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PolylineCodec {

    /**
     * Encodes a list of LatLng points into a Google Encoded Polyline string.
     *
     * @param points the list of points to encode
     * @return the encoded polyline string, or null if points is null or empty
     */
    public static String encode(List<LatLng> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return new EncodedPolyline(points).getEncodedPath();
    }

    /**
     * Decodes a Google Encoded Polyline string into a list of LatLng points.
     *
     * @param encodedPolyline the encoded polyline string
     * @return list of decoded LatLng points, or empty list if input is null or empty
     */
    public static List<LatLng> decode(String encodedPolyline) {
        if (encodedPolyline == null || encodedPolyline.isEmpty()) {
            return List.of();
        }
        return new EncodedPolyline(encodedPolyline).decodePath();
    }
}
