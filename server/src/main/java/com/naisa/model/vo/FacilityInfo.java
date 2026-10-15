package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.JavaBean;

/**
 * Contains known facility attributes.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacilityInfo {
  private String surfaceType;

  private Boolean isIndoor;

  private Boolean hasLighting;

  public String getSurfaceType() {
    return surfaceType;
  }

  public void setSurfaceType(final String surfaceType) {
    this.surfaceType = surfaceType;
  }

  @JsonProperty("isIndoor")
  public Boolean isIndoor() {
    return isIndoor;
  }

  public void setIsIndoor(final Boolean isIndoor) {
    this.isIndoor = isIndoor;
  }

  public Boolean isHasLighting() {
    return hasLighting;
  }

  public void setHasLighting(final Boolean hasLighting) {
    this.hasLighting = hasLighting;
  }
}
