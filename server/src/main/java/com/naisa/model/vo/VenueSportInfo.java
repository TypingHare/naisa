package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.beans.JavaBean;
import java.math.BigDecimal;

/**
 * Contains one sport and its stored price and field data.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VenueSportInfo {
  private String sport;

  private BigDecimal priceUsd;

  private FacilityInfo facility;

  public String getSport() {
    return sport;
  }

  public void setSport(final String sport) {
    this.sport = sport;
  }

  public BigDecimal getPriceUsd() {
    return priceUsd;
  }

  public void setPriceUsd(final BigDecimal priceUsd) {
    this.priceUsd = priceUsd;
  }

  public FacilityInfo getFacility() {
    return facility;
  }

  public void setFacility(final FacilityInfo facility) {
    this.facility = facility;
  }
}
