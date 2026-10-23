package com.naisa.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request to report the crowd level at a venue.
 *
 * <p>The report may apply to a specific sport or the entire venue.
 *
 * @author Emily Wang, Codex
 */
@JavaBean
@Getter
@Setter
public class ReportCrowdRequest {
  @NotBlank
  @Size(max = 128)
  private String externalUserId;

  @NotBlank
  @Pattern(regexp = "EMPTY|LIGHT|MODERATE|BUSY|PACKED")
  private String crowdLevel;

  @Positive
  private Long sportId;
}
