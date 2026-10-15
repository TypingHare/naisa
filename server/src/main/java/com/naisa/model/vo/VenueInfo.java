package com.naisa.model.vo;

import com.naisa.model.dto.Location;
import java.beans.JavaBean;
import java.util.List;

/**
 * Contains stored details for one venue.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class VenueInfo {
  private String venueId;

  private String venueName;

  private String address;

  private Location location;

  private String timezone;

  private List<VenueSportInfo> sports = List.of();

  private List<VenueOpeningHours> openingHours = List.of();

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

  public String getAddress() {
    return address;
  }

  public void setAddress(final String address) {
    this.address = address;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(final String timezone) {
    this.timezone = timezone;
  }

  public List<VenueSportInfo> getSports() {
    return sports;
  }

  public void setSports(final List<VenueSportInfo> sports) {
    this.sports = sports;
  }

  public List<VenueOpeningHours> getOpeningHours() {
    return openingHours;
  }

  public void setOpeningHours(final List<VenueOpeningHours> openingHours) {
    this.openingHours = openingHours;
  }
}
