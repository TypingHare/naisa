package com.naisa.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries a user's rating and written feedback about a venue or one of its sports.
 *
 * <p>An omitted or null sport identifier selects the entire venue. A supplied identifier selects a
 * sport offered at that venue. The external user identifier is scoped to the authenticated client.
 *
 * <p>Ratings must be whole numbers from one through five, and comments must contain non-whitespace
 * text. Decimal input is preserved until validation so that a fractional rating cannot be silently
 * truncated to an integer.
 *
 * @author Vincent Lai, Codex
 */
@JavaBean
@Getter
@Setter
public class RateVenueRequest {
  @JsonProperty("externalUserID")
  @NotBlank
  @Size(max = 128)
  private String externalUserId;

  @Positive
  private Long sportId;

  @NotNull
  @DecimalMin("1")
  @DecimalMax("5")
  @Digits(integer = 1, fraction = 0)
  private BigDecimal rating;

  @NotBlank
  @Size(max = 2000)
  private String comment;
}