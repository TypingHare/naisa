package com.naisa.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Carries the query parameters for a venue search.
 *
 * <p>Coordinates identify the search origin. Maximum distance is in kilometers, and budget
 * is the maximum hourly budget. An omitted budget defaults to zero (free venues only).
 * Decimal values follow the existing request model and rejects NaN or infinity.
 *
 * <p>The date uses YYYY-MM-DD format, parsed with the Java pattern uuuu-MM-dd. The time uses the
 * 24-hour HH:mm format. Assumes America/New_York time zone since all venues are in NYC. 
 * After field validation succeeds, the service must reject a combined date and time that is in 
 * the past and check that the sport exists.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindVenuesRequest {
  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private BigDecimal latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private BigDecimal longitude;

  @NotNull
  @Positive
  private Long sportId;

  @NotNull
  @DateTimeFormat(pattern = "uuuu-MM-dd")
  private LocalDate date;

  @NotNull
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime time;

  @NotNull
  @DecimalMin("0")
  private BigDecimal maxDistance;

  @NotNull
  @DecimalMin("0")
  private BigDecimal budget = BigDecimal.ZERO;
}
