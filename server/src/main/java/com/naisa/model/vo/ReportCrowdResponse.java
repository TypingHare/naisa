package com.naisa.model.vo;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the result of reporting a venue's crowd level.
 *
 * @author Emily Wang
 */
@Getter
@AllArgsConstructor
public class ReportCrowdResponse {
  private Long reportId;
  private String crowdLevel;
  private Instant createdAt;
  private boolean replaced;
}
