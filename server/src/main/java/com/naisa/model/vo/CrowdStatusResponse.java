package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes the current crowd status and supporting reports for a venue.
 *
 * <p>
 * Without recent reports, crowd level and latest report time are null, and source is {@code
 * UNKNOWN}.
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
