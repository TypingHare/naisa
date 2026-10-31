package com.naisa.model.dto;

import com.naisa.util.EarthCoordinates.WithLatitudeAndLongitude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Parameters for creating a venue.
 *
 * <p>This request does not include sports or opening intervals.
 *
 * @author Vincent Lai, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class CreateVenueRequest implements WithLatitudeAndLongitude {
  @NotBlank
  @Size(max = 255)
  private String name;

  @NotBlank
  @Size(max = 255)
  private String address;

  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;

  @NotBlank
  @Size(max = 32)
  private String timezone;
}
