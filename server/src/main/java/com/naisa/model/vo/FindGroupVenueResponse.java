package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one open venue returned by a group search.
 *
 * <p>Distances are in kilometers. Member distances follow the input location order, and
 * average distance is their arithmetic average. Price and indoor status apply to the requested
 * sport. Every returned venue is open at the requested date and time.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindGroupVenueResponse {
  private Long venueId;
  private String name;
  private double averageDistanceKm;
  private List<Double> memberDistancesKm;
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private boolean available;

  /**
   * Creates a response for one venue that matches the group search parameters.
   *
   * @param venueId identifier of the venue
   * @param name name of the venue
   * @param averageDistanceKm average distance from all group members in kilometers
   * @param memberDistancesKm member distances in input location order
   * @param hourlyPrice hourly price in dollars for the requested sport
   * @param isIndoor whether the requested sport's facility is indoors
   * @param available whether the venue is open at the requested date and time
   * @author Andrew Hsu, Codex
   */
  public FindGroupVenueResponse(
      final Long venueId,
      final String name,
      final double averageDistanceKm,
      final List<Double> memberDistancesKm,
      final BigDecimal hourlyPrice,
      final Boolean isIndoor,
      final boolean available) {
    this.venueId = venueId;
    this.name = name;
    this.averageDistanceKm = averageDistanceKm;
    this.memberDistancesKm = memberDistancesKm;
    this.hourlyPrice = hourlyPrice;
    this.isIndoor = isIndoor;
    this.available = available;
  }
}
