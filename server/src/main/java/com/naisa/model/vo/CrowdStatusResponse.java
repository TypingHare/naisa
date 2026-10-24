package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the current crowd status of a venue.
 *
 * <p>If there are no recent reports, {@code crowdLevel} and {@code latestReportAt} are null and
 * {@code source} is {@code UNKNOWN}.
 *
 * @author Emily Wang, Claude Code
 */
@JavaBean
@Getter
@Setter
public class CrowdStatusResponse {
  private Long venueId;
  private String crowdLevel;
  private int reportCount;
  private Instant latestReportAt;
  private String source;
}
