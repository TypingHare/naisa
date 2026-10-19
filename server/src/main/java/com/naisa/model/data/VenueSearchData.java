package com.naisa.model.data;

import java.beans.JavaBean;
import java.util.List;

/**
 * Reports whether the sport exists and carries its stored venue candidates.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
public class VenueSearchData {
  private Boolean sportFound = false;

  private List<VenueCandidate> venues = List.of();

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
