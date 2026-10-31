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
 * Parameters for searching for venues for one to ten members.
 *
 * <p>The maximum distance applies to each member and is measured in kilometers. The budget is the
 * maximum hourly price and defaults to zero. Date and time use America/New_York.
 *
 * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class SearchVenuesRequest {
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
