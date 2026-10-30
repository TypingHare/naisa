package com.naisa.model.vo;

import java.beans.JavaBean;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a venue value object.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueVo {
  private Long venueId;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;
  private String timezone;
  private List<SportVo> sports = List.of();
  private List<OpeningIntervalVo> openingIntervals = List.of();
}
