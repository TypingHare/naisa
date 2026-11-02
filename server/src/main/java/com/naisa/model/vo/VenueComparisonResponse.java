package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one venue's distance, hourly price, availability, and crowd level in a comparison.
 *
 * <p>
 * Distance is in kilometers. Opening status is false when hours are unavailable. Crowd score and
 * level are null when recent eligible reports are unavailable.
 *
 * @author Emily Wang, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueComparisonResponse {
    private Long venueId;
    private String name;
    private Double distanceKm;
    private BigDecimal hourlyPrice;
    private Boolean isIndoor;
    private Boolean openNow;
    private Double crowdLevelScore;
    private String crowdLevel;
}
