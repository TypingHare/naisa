package com.naisa.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.util.List;

/**
 * Contains the locations and group search inputs.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class GroupSearchRequest extends SearchCriteria {
  @NotNull
  @Size(min = 2, max = 20)
  private List<@NotNull @Valid Location> locations;

  @DecimalMin("0")
  @DecimalMax("1.7976931348623157E308")
  private Double distanceFromCenter;

  public List<@NotNull @Valid Location> getLocations() {
    return locations;
  }

  public void setLocations(final List<@NotNull @Valid Location> locations) {
    this.locations = locations;
  }

  public Double getDistanceFromCenter() {
    return distanceFromCenter;
  }

  public void setDistanceFromCenter(final Double distanceFromCenter) {
    this.distanceFromCenter = distanceFromCenter;
  }
}
