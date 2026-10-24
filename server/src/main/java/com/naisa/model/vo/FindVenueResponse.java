package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue returned by a venue search.
 *
 * <p>Distances are in kilometers. Distance is measured from the average member coordinates.
 * Member distances follow the input order; their arithmetic average determines result order.
 * All three distance values are equal for a single origin. Price and indoor status apply to the
 * requested sport. Only open venues are returned, so availability is always true.
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

  /**
   * Creates a response for one venue and a single search origin.
   *
   * <p>For multiple members, the caller replaces the member distances and their average.
   *
   * @param venueId     identifier of the venue
   * @param name        name of the venue
   * @param distanceKm  distance from the search origin in kilometers
   * @param hourlyPrice hourly price in dollars for the requested sport
   * @param isIndoor    whether the requested sport's facility is indoors
   * @param available   whether the venue is open at the requested date and time
   * @author Andrew Hsu, Codex
   */
  public FindVenueResponse(
      final Long venueId,
      final String name,
      final double distanceKm,
      final BigDecimal hourlyPrice,
      final Boolean isIndoor,
      final boolean available) {
    this.venueId = venueId;
    this.name = name;
    this.distanceKm = distanceKm;
    this.averageDistanceKm = distanceKm;
    this.memberDistancesKm = List.of(distanceKm);
    this.hourlyPrice = hourlyPrice;
    this.isIndoor = isIndoor;
    this.available = available;
  }
}
