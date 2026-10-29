package com.naisa.model.dto;

import com.naisa.util.EarthCoordinates.WithLatitudeAndLongitude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request parameters for comparing venues that offer the same sport.
 *
 * <p>Includes the venue IDs, requested sport, and user's location. The venue IDs must be distinct
 * and contain between two and five entries.
 *
 * @author Emily Wang, Codex, Vincent Lai, Codex
 */
@Getter
@Setter
public class VenueComparisonRequest implements WithLatitudeAndLongitude {
  @NotNull
  @Size(min = 2, max = 5)
  private List<@NotNull @Positive Long> venueIds;

  @NotNull @Positive private Long sportId;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private Double latitude;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private Double longitude;
}
