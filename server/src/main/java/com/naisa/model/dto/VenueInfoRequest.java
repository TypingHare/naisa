package com.naisa.model.dto;

import java.beans.JavaBean;

/**
 * Selects one venue by ID or name.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class VenueInfoRequest {
  private String venueId;

  private String venueName;

  public String getVenueId() {
    return venueId;
  }

  public void setVenueId(final String venueId) {
    this.venueId = venueId;
  }

  public String getVenueName() {
    return venueName;
  }

  public void setVenueName(final String venueName) {
    this.venueName = venueName;
  }
}
