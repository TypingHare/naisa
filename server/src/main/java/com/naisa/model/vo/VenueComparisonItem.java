package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the comparison details of a single venue.
 *
 * <p>The {@code openNow} field is null if the venue has no opening hours. The {@code crowdLevel}
 * field is null if the venue has no recent crowd reports.
 *
 * @author Emily Wang, Codex
 */
@JavaBean
@Getter
@Setter
public class VenueComparisonItem {
  private Long venueId;
  private String name;
  private double distanceKm;
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private Boolean openNow;
  private String crowdLevel;
}
