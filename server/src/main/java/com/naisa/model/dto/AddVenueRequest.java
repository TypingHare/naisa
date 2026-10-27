package com.naisa.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries the information required to submit a venue.
 *
 * <p>The calling client supplies the venue's coordinates. Authentication supplies the submitting
 * client identity separately. This request does not create sports, prices, or opening intervals.
 *
 * <p>The timezone must fit the database column and identify a timezone recognized by the
 * application. Its meaning is checked by the service after field validation succeeds.
 *
 * @author Vincent Lai, Codex
 */
@JavaBean
@Getter
@Setter
public class AddVenueRequest {
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
