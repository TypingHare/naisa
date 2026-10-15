package com.naisa.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;

/**
 * Contains the inputs for a single-location search.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class VenueSearchRequest extends SearchCriteria {
  @NotNull @Valid private Location location;

  @NotNull
  @DecimalMin("0") /* reject negative distances */
  @DecimalMax("1.7976931348623157E308") /* reject infinite distances */
  private Double maxDistance;

  public Location getLocation() {
    return location;
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  public Double getMaxDistance() {
    return maxDistance;
  }

  public void setMaxDistance(final Double maxDistance) {
    this.maxDistance = maxDistance;
  }
}
