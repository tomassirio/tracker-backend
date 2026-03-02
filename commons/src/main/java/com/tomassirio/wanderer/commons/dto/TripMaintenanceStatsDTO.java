package com.tomassirio.wanderer.commons.dto;

/**
 * DTO containing trip maintenance statistics for the admin dashboard. Includes both polyline and
 * geocoding coverage metrics.
 *
 * @param totalTrips total number of trips in the system
 * @param tripsWithPolyline number of trips that have an encoded polyline
 * @param tripsWithMultipleLocations number of trips that have 2 or more location updates
 * @param tripsMissingPolyline number of trips with 2+ locations but no polyline
 * @param totalUpdates total number of trip updates across all trips
 * @param updatesWithGeocoding number of trip updates that have city/country populated
 * @param updatesMissingGeocoding number of trip updates missing city/country
 * @since 0.9.0
 */
public record TripMaintenanceStatsDTO(
        long totalTrips,
        long tripsWithPolyline,
        long tripsWithMultipleLocations,
        long tripsMissingPolyline,
        long totalUpdates,
        long updatesWithGeocoding,
        long updatesMissingGeocoding) {}
