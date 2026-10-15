package com.naisa.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;

/**
 * Contains geographic coordinates.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class Location {
  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(final Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(final Double longitude) {
    this.longitude = longitude;
  }
}
