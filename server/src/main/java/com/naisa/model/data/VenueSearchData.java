package com.naisa.model.data;

import java.beans.JavaBean;
import java.util.List;

/**
 * Reports whether the data adapter is ready and whether the sport exists.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
public class VenueSearchData {
  private Boolean implemented = false;

  private Boolean sportFound = false;

  private List<VenueCandidate> venues = List.of();

  public Boolean isImplemented() {
    return implemented;
  }

  public void setImplemented(final Boolean implemented) {
    this.implemented = implemented;
  }

  public Boolean isSportFound() {
    return sportFound;
  }

  public void setSportFound(final Boolean sportFound) {
    this.sportFound = sportFound;
  }

  public List<VenueCandidate> getVenues() {
    return venues;
  }

  public void setVenues(final List<VenueCandidate> venues) {
    this.venues = venues;
  }
}
