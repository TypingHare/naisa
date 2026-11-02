package com.naisa.model.vo;

import java.beans.JavaBean;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains venue details and separate ID rankings by distance, price, and crowd score.
 *
 * <p>
 * Details retain request order. Rankings are ascending, with ties broken by venue ID. The crowd
 * ranking excludes unknown crowd levels and is empty when none are known.
 *
 * @author Emily Wang, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class CompareVenuesResponse {
    private List<VenueComparisonResponse> venueComparisonResponses;
    private List<Long> nearestVenueIds;
    private List<Long> cheapestVenueIds;
    private List<Long> leastCrowdedVenueIds;
}
