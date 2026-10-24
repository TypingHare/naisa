package com.naisa.common;

/**
 * Calculates direct geographic distances for venue searches and comparisons.
 *
 * @author Emily Wang, Codex, Andrew Hsu, Codex
 */
public final class VenueDistance {
  private static final double EARTH_RADIUS_KM = 6371.0;

  private VenueDistance() {}

  /**
   * Calculates distance between two coordinates with the Haversine formula.
   *
   * @param originLatitude origin latitude in degrees
   * @param originLongitude origin longitude in degrees
   * @param venueLatitude venue latitude in degrees
   * @param venueLongitude venue longitude in degrees
   * @return distance in kilometers
   */
  public static double calculateDistanceKm(
      final double originLatitude,
      final double originLongitude,
      final double venueLatitude,
      final double venueLongitude) {
    final double latitudeDifference = Math.toRadians(venueLatitude - originLatitude);
    final double longitudeDifference = Math.toRadians(venueLongitude - originLongitude);
    final double haversine =
        Math.pow(Math.sin(latitudeDifference / 2), 2)
            + Math.cos(Math.toRadians(originLatitude))
                * Math.cos(Math.toRadians(venueLatitude))
                * Math.pow(Math.sin(longitudeDifference / 2), 2);
    // Keep rounding errors within the square roots' valid range.
    final double boundedHaversine = Math.max(0, Math.min(1, haversine));
    final double centralAngle =
        2 * Math.atan2(Math.sqrt(boundedHaversine), Math.sqrt(1 - boundedHaversine));
    return EARTH_RADIUS_KM * centralAngle;
  }
}
