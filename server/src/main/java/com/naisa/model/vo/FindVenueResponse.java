package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue returned by a venue search.
 *
 * <p>Distance is measured from the search origin in km. Indoor status applies 
 * to that sport's facility. Availability indicates whether the venue is open 
 * at the requested date and time. The search excludes closed venues, so every 
 * returned result has availability set to true.
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
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private boolean available;

  /**
   * Creates a response for one venue that matches the search parameters.
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
    this.hourlyPrice = hourlyPrice;
    this.isIndoor = isIndoor;
    this.available = available;
  }
}
