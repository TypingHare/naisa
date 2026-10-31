package com.naisa.model.vo;

import java.beans.JavaBean;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes a venue comparison and identifies the nearest, cheapest, and least crowded venues.
 *
 * <p>The least crowded venue ID is null when no venue has recent crowd reports.
 *
 * @author Emily Wang, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class CompareVenuesResponse {
  private List<VenueComparisonResponse> venues;
  private Long nearestVenueId;
  private Long cheapestVenueId;
  private Long leastCrowdedVenueId;
}
