package com.naisa.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
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
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Carries the query parameters for a venue search.
 *
 * <p>Supply either latitude and longitude for one origin, or indexed locations for 1 to 10
 * members. Do not mix the two forms. List indices identify members in the response. Maximum
 * distance is in kilometers from the average latitude and longitude. Budget is the maximum
 * hourly price and defaults to zero. Decimal values cannot represent NaN or infinity.
 *
 * <p>The date uses YYYY-MM-DD format, parsed with the Java pattern uuuu-MM-dd. The time uses a
 * 24-hour clock. The standard parser accepts HH:mm and ISO local times with seconds and fractional
 * seconds. All venues are in New York City, so date and time use America/New_York. After field
 * validation succeeds, the service must reject a combined date and time that is in the past and
 * check that the sport exists.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindVenuesRequest {
  @DecimalMin("-90")
  @DecimalMax("90")
  private BigDecimal latitude;

  @DecimalMin("-180")
  @DecimalMax("180")
  private BigDecimal longitude;

  @Size(min = 1, max = 10)
  private List<@NotNull @Valid GroupMemberLocation> locations;

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

  /**
   * Requires one coordinate pair or a location list, with no mixture of the two forms.
   *
   * @return true when exactly one complete location form is supplied
   */
  @AssertTrue(message = "Supply latitude and longitude, or locations, but not both.")
  public boolean isLocationSelectionValid() {
    if (locations != null) {
      return latitude == null && longitude == null;
    }
    return latitude != null && longitude != null;
  }
}
