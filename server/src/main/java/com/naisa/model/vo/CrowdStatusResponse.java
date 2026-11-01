package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes the current crowd status and supporting reports for a venue.
 *
 * <p>
 * Without recent eligible reports, the crowd-level score, crowd level, and latest report time are
 * null, the report count is zero, and the source is {@code UNKNOWN}.
 *
 * @author Emily Wang, Claude Code, James Chen
 */
@JavaBean
@Getter
@Setter
public class CrowdStatusResponse {
    private Long venueId;
    private String source;
    private Integer reportCount;
    private Instant latestReportAt;
    private Double crowdLevelScore;
    private String crowdLevel;
}
