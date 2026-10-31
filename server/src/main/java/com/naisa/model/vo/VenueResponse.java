package com.naisa.model.vo;

import java.beans.JavaBean;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the information of a venue.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueResponse {
	private Long venueId;
	private String name;
	private String address;
	private Double latitude;
	private Double longitude;
	private String timezone;
	private List<VenueSportResponse> sports = List.of();
	private List<OpeningIntervalResponse> openingIntervals = List.of();
}
