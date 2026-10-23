package com.naisa.model.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comparison results for the selected venues.
 *
 * <p>Venues are sorted by distance, and the best section identifies
 * the nearest, cheapest, and least crowded venues when available.
 *
 * @author Emily Wang, Codex
 */
public record CompareVenuesResponse(
    List<VenueComparison> venues,
    BestVenues best) {

  /**
   * Comparison details for a single venue.
   *
   * @author Emily Wang
   */
  public record VenueComparison(
      Long venueId,
      String name,
      double distanceKm,
      BigDecimal hourlyPrice,
      Boolean isIndoor,
      Boolean openNow,
      String crowdLevel) {}

  /**
   * IDs of the best venues in each category.
   *
   * @author Emily Wang
   */
  public record BestVenues(
      Long nearest,
      Long cheapest,
      Long leastCrowded) {}
}
