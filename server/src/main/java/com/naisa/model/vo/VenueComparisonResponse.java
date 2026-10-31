package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue's distance, hourly price, availability, and crowd level in a comparison.
 *
 * <p>
 * Distance is in kilometers. Opening status is null when hours are unavailable; crowd level is null
 * when recent reports are unavailable.
 *
 * @author Emily Wang, Codex
 */
@JavaBean
@Getter
@Setter
public class VenueComparisonResponse {
	private Long venueId;
	private String name;
	private double distanceKm;
	private BigDecimal hourlyPrice;
	private Boolean isIndoor;
	private Boolean openNow;
	private String crowdLevel;
}
