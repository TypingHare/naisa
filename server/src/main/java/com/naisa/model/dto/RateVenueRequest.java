package com.naisa.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Parameters for rating a venue or a sport offered at that venue.
 *
 * <p>Ratings range from 1 to 5 and require a comment. A null sport ID selects the entire venue. The
 * external user ID is scoped to the authenticated client.
 *
 * @author Vincent Lai, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class RateVenueRequest {
  @JsonProperty("externalUserID")
  @NotBlank
  @Size(max = 128)
  private String externalUserId;

  @Positive private Long sportId;

  @NotNull
  @Min(1)
  @Max(5)
  private Short rating;

  @NotBlank
  @Size(max = 2000)
  private String comment;
}
