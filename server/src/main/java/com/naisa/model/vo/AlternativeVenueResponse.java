package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one alternative venue and its suggested search parameters.
 *
 * <p>
 * Date and time use America/New_York. Maximum distance is in kilometers; budget is the maximum
 * hourly price.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class AlternativeVenueResponse {
	private VenueSearchResultResponse venue;
	private LocalDate date;
	private LocalTime time;
	private BigDecimal maxDistance;
	private BigDecimal budget;
}
