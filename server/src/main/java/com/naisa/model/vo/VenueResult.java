package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.beans.JavaBean;
import java.util.List;

/**
 * Contains a venue that meets the effective search criteria.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VenueResult {
  private String venueId;

  private String venueName;

  private String sport;

  private Double distanceKm;

  private Double distanceFromCenterKm;

  private Double averageDistanceKm;

  private List<Double> memberDistancesKm;

  private PriceEstimate estimatedPrice;

  private FacilityInfo facility;

  private AvailabilityInfo availability;

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

  public String getSport() {
    return sport;
  }

  public void setSport(final String sport) {
    this.sport = sport;
  }

  public Double getDistanceKm() {
    return distanceKm;
  }

  public void setDistanceKm(final Double distanceKm) {
    this.distanceKm = distanceKm;
  }

  public Double getDistanceFromCenterKm() {
    return distanceFromCenterKm;
  }

  public void setDistanceFromCenterKm(final Double distanceFromCenterKm) {
    this.distanceFromCenterKm = distanceFromCenterKm;
  }

  public Double getAverageDistanceKm() {
    return averageDistanceKm;
  }

  public void setAverageDistanceKm(final Double averageDistanceKm) {
    this.averageDistanceKm = averageDistanceKm;
  }

  public List<Double> getMemberDistancesKm() {
    return memberDistancesKm;
  }

  public void setMemberDistancesKm(final List<Double> memberDistancesKm) {
    this.memberDistancesKm = memberDistancesKm;
  }

  public PriceEstimate getEstimatedPrice() {
    return estimatedPrice;
  }

  public void setEstimatedPrice(final PriceEstimate estimatedPrice) {
    this.estimatedPrice = estimatedPrice;
  }

  public FacilityInfo getFacility() {
    return facility;
  }

  public void setFacility(final FacilityInfo facility) {
    this.facility = facility;
  }

  public AvailabilityInfo getAvailability() {
    return availability;
  }

  public void setAvailability(final AvailabilityInfo availability) {
    this.availability = availability;
  }
}
