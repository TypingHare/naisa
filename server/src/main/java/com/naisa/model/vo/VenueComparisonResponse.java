package com.naisa.model.vo;

import java.beans.JavaBean;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the result of comparing venues.
 *
 * <p>Venues are sorted by distance, nearest first. The nearest, cheapest, and least crowded venue
 * IDs are provided separately. The least crowded venue ID is null if no venue has recent crowd
 * reports.
 *
 * @author Emily Wang, Codex
 */
@JavaBean
@Getter
@Setter
public class VenueComparisonResponse {
  private List<VenueComparisonItem> venues;
  private Long nearestVenueId;
  private Long cheapestVenueId;
  private Long leastCrowdedVenueId;
}
