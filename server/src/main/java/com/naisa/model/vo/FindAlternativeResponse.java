package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes a venue and the suggested search values that make it a valid alternative.
 *
 * <p>The date and time use America/New_York. Maximum distance is in kilometers, and budget is the
 * maximum hourly price in dollars. The nested venue contains the actual distance and price.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindAlternativeResponse {
  private VenueSearchResponse venue;
  private LocalDate date;
  private LocalTime time;
  private BigDecimal maxDistance;
  private BigDecimal budget;

  /**
   * Creates one alternative with its suggested search values.
   *
   * @param venue venue details evaluated at the suggested date and time
   * @param date suggested local date
   * @param time suggested local time
   * @param maxDistance suggested radius in kilometers
   * @param budget suggested maximum hourly price in dollars
   * @author Andrew Hsu, Codex
   */
  public FindAlternativeResponse(
      final VenueSearchResponse venue,
      final LocalDate date,
      final LocalTime time,
      final BigDecimal maxDistance,
      final BigDecimal budget) {
    this.venue = venue;
    this.date = date;
    this.time = time;
    this.maxDistance = maxDistance;
    this.budget = budget;
  }
}
