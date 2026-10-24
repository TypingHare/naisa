package com.naisa.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries the JSON body for a group venue search.
 *
 * <p>Location order identifies members in the response. The radius is measured in kilometers
 * from the average latitude and longitude. Budget is the maximum hourly price in dollars.
 * Omitted radius and budget values default to 2 kilometers and zero dollars, respectively.
 *
 * <p>JSON date and time values use ISO local formats: YYYY-MM-DD for the date and a 24-hour
 * time with optional seconds and fractional seconds. The service interprets them in
 * America/New_York, rejects past requests, and checks that the sport exists.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindGroupVenuesRequest {
  @NotNull
  @Size(min = 2, max = 10)
  private List<@NotNull @Valid GroupMemberLocation> locations;

  @NotNull
  @Positive
  private Long sportId;

  @NotNull
  private LocalDate date;

  @NotNull
  private LocalTime time;

  @NotNull
  @DecimalMin("0")
  private BigDecimal distanceFromCenter = BigDecimal.valueOf(2);

  @NotNull
  @DecimalMin("0")
  private BigDecimal budget = BigDecimal.ZERO;
}
