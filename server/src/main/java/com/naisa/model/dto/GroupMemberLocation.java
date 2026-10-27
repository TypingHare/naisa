package com.naisa.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries the coordinates of one member in a group venue search.
 *
 * @author Andrew Hsu, Codex, Vincent Lai, Codex
 */
@JavaBean
@Getter
@Setter
public class GroupMemberLocation {
  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;
}
