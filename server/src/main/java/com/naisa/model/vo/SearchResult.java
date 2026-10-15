package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.SearchCriteria;
import java.beans.JavaBean;
import java.util.List;

/**
 * Identifies exact matches, alternatives, or an empty search.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {
  private String matchType;

  private SearchCriteria requestedCriteria;

  private SearchCriteria effectiveCriteria;

  private List<String> relaxedFields = List.of();

  private Boolean searchLimitReached = false;

  private Location center;

  private List<VenueResult> venues = List.of();

  public String getMatchType() {
    return matchType;
  }

  public void setMatchType(final String matchType) {
    this.matchType = matchType;
  }

  public SearchCriteria getRequestedCriteria() {
    return requestedCriteria;
  }

  public void setRequestedCriteria(final SearchCriteria requestedCriteria) {
    this.requestedCriteria = requestedCriteria;
  }

  public SearchCriteria getEffectiveCriteria() {
    return effectiveCriteria;
  }

  public void setEffectiveCriteria(final SearchCriteria effectiveCriteria) {
    this.effectiveCriteria = effectiveCriteria;
  }

  public List<String> getRelaxedFields() {
    return relaxedFields;
  }

  public void setRelaxedFields(final List<String> relaxedFields) {
    this.relaxedFields = relaxedFields;
  }

  public Boolean isSearchLimitReached() {
    return searchLimitReached;
  }

  public void setSearchLimitReached(final Boolean searchLimitReached) {
    this.searchLimitReached = searchLimitReached;
  }

  public Location getCenter() {
    return center;
  }

  public void setCenter(final Location center) {
    this.center = center;
  }

  public List<VenueResult> getVenues() {
    return venues;
  }

  public void setVenues(final List<VenueResult> venues) {
    this.venues = venues;
  }
}
