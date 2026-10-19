package com.naisa.model.data;

import com.naisa.model.dto.Location;
import com.naisa.model.vo.FacilityInfo;
import java.beans.JavaBean;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contains joined venue, sport, price, and hours data.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
public class VenueCandidate {
  private Long venueId;

  private String venueName;

  private String sport;

  private Location location;

  private String timezone;

  private BigDecimal price;

  private String currency;

  private String priceUnit;

  private FacilityInfo facility;

  private List<OpeningInterval> openingHours = List.of();

  public Long getVenueId() {
    return venueId;
  }

  public void setVenueId(final Long venueId) {
    this.venueId = venueId;
  }

  public String getVenueName() {
    return venueName;
  }

  public void setVenueName(final String venueName) {
    this.venueName = venueName;
  }

  public String getSport() {
    return sport;
  }

  public void setSport(final String sport) {
    this.sport = sport;
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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(final BigDecimal price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(final String currency) {
    this.currency = currency;
  }

  public String getPriceUnit() {
    return priceUnit;
  }

  public void setPriceUnit(final String priceUnit) {
    this.priceUnit = priceUnit;
  }

  public FacilityInfo getFacility() {
    return facility;
  }

  public void setFacility(final FacilityInfo facility) {
    this.facility = facility;
  }

  public List<OpeningInterval> getOpeningHours() {
    return openingHours;
  }

  public void setOpeningHours(final List<OpeningInterval> openingHours) {
    this.openingHours = openingHours;
  }
}
