package com.naisa.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contains the common search inputs.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class SearchCriteria {
  @NotBlank
  @Size(max = 50)
  private String sport;

  @NotNull
  @JsonFormat(pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
  private LocalDate date;

  @NotNull
  @JsonFormat(pattern = "HH:mm", lenient = OptBoolean.FALSE)
  private LocalTime time;

  @NotNull
  @DecimalMin("0")
  @Digits(integer = 8, fraction = 2)
  private BigDecimal budget = BigDecimal.ZERO;

  public String getSport() {
    return sport;
  }

  public void setSport(final String sport) {
    this.sport = sport;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(final LocalDate date) {
    this.date = date;
  }

  public LocalTime getTime() {
    return time;
  }

  public void setTime(final LocalTime time) {
    this.time = time;
  }

  public BigDecimal getBudget() {
    return budget;
  }

  public void setBudget(final BigDecimal budget) {
    this.budget = budget;
  }
}
