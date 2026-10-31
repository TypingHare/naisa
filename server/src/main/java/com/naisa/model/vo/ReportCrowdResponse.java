package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes a submitted crowd report.
 *
 * @author Emily Wang
 */
@JavaBean
@Getter
@Setter
public class ReportCrowdResponse {
	private Long reportId;
	private String crowdLevel;
	private Instant createdAt;
}
