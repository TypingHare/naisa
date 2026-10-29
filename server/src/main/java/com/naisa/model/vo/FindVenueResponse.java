package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue returned by a venue search.
 *
 * <p>Distances are in kilometers. Distance is measured from the average member coordinates. Member
 * distances follow the input order; their arithmetic average determines result order. All three
 * distance values are equal for a single origin. Price and indoor status apply to the requested
 * sport. Only open venues are returned, so availability is always true.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindVenueResponse {
  private Long venueId;
  private String name;
  private double distanceKm;
  private double averageDistanceKm;
  private List<Double> memberDistancesKm;
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private boolean available;
}
