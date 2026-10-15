package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.beans.JavaBean;
import java.math.BigDecimal;

/**
 * Contains the price for one court-hour.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceEstimate {
  private BigDecimal amount;

  private String currency = "USD";

  private String priceUnit = "PER_COURT_HOUR";

  private Integer durationMinutes = 60;

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(final BigDecimal amount) {
    this.amount = amount;
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

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(final Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }
}
