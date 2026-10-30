package com.naisa.model.dto;

import com.naisa.model.common.EarthCoordinate;
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
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Carries the query parameters for a venue search.
 *
 * <p>Supply between 1 and 10 locations as geographic coordinates. List indices identify members in
 * the {@code VenueSearchResponse}.
 *
 * <p>{@code maxDistance} is in kilometers. {@code budget} is the maximum hourly price and defaults
 * to zero.
 *
 * <p>The date uses YYYY-MM-DD format, parsed with the Java pattern uuuu-MM-dd. The time uses a
 * 24-hour clock. The standard parser accepts HH:mm and ISO local times with seconds and fractional
 * seconds. All venues are in New York City, so date and time use America/New_York. After field
 * validation succeeds, the service must reject a combined date and time that is in the past and
 * check that the sport exists.
 *
 * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueSearchRequest {
  @NotNull
  @Size(min = 1, max = 10)
  private List<@NotNull @Valid EarthCoordinate> coordinates;

  @NotNull @Positive private Long sportId;

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
