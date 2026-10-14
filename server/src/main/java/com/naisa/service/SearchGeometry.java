package com.naisa.service;

import com.naisa.exception.VenueSearchException;
import com.naisa.model.dto.Location;
import java.util.List;

/**
 * Computes geographic distance and a group center.
 *
 * @author Andrew Hsu
 */
public final class SearchGeometry {
  private static final double EARTH_RADIUS_KM = 6371.0088;
  private static final double CENTER_TOLERANCE = 1.0e-12;

  private SearchGeometry() {}

  /**
   * Computes great-circle distance in kilometers.
   *
   * @author Andrew Hsu
   */
  public static double distance(final Location origin, final Location destination) {
    final double latitude = Math.toRadians(origin.getLatitude());
    final double otherLatitude = Math.toRadians(destination.getLatitude());
    final double latitudeDelta = otherLatitude - latitude;
    final double longitudeDelta =
        Math.toRadians(destination.getLongitude() - origin.getLongitude());
    final double haversine =
        Math.pow(Math.sin(latitudeDelta / 2), 2)
            + Math.cos(latitude)
                * Math.cos(otherLatitude)
                * Math.pow(Math.sin(longitudeDelta / 2), 2);
    return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(Math.clamp(haversine, 0, 1)));
  }

  /**
   * Computes a spherical center while preserving duplicate members' weights.
   *
   * @author Andrew Hsu
   */
  public static Location center(final List<Location> locations) {
    double horizontal = 0;
    double vertical = 0;
    double polar = 0;
    for (final Location location : locations) {
      final double latitude = Math.toRadians(location.getLatitude());
      final double longitude = Math.toRadians(location.getLongitude());
      horizontal += Math.cos(latitude) * Math.cos(longitude);
      vertical += Math.cos(latitude) * Math.sin(longitude);
      polar += Math.sin(latitude);
    }
    final double magnitude =
        Math.sqrt(horizontal * horizontal + vertical * vertical + polar * polar);
    if (magnitude / locations.size() < CENTER_TOLERANCE) {
      throw VenueSearchException.UNDEFINED_GROUP_CENTER.clone();
    }
    final Location center = new Location();
    center.setLatitude(Math.toDegrees(Math.atan2(polar, Math.hypot(horizontal, vertical))));
    center.setLongitude(Math.toDegrees(Math.atan2(vertical, horizontal)));
    return center;
  }
}
