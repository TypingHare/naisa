package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue returned by a venue search.
 *
 * <p>
 * Member distances are in kilometers and follow request coordinate order. Price, indoor status, and
 * lighting availability apply to the requested sport.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueSearchResultResponse {
	private Long venueId;
	private String name;
	private List<Double> memberDistancesKm;
	private BigDecimal hourlyPrice;
	private Boolean isIndoor;
	private Boolean hasLighting;
}
