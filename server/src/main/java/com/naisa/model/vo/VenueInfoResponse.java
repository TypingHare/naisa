package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes a venue's location, offered sports, and weekly opening intervals.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class VenueInfoResponse {
  private Long venueId;
  private String name;
  private String address;
  private double latitude;
  private double longitude;
  private String timezone;
  private List<SportInfo> sports = List.of();
  private List<OpeningIntervalInfo> openingIntervals = List.of();

  /**
   * Describes a sport offered at this venue, with its hourly price in dollars and indoor status.
   *
   * @author Andrew Hsu, Codex
   */
  @JavaBean
  @Getter
  @Setter
  public static class SportInfo {
    private Long sportId;
    private String name;
    private BigDecimal hourlyPrice;
    private Boolean isIndoor;
  }

  /**
   * Describes one weekly opening interval in the venue's local time.
   *
   * @author Andrew Hsu, Codex
   */
  @JavaBean
  @Getter
  @Setter
  public static class OpeningIntervalInfo {
    private DayOfWeek dayOfWeek;
    private LocalTime opensAt;
    private LocalTime closesAt;
  }
}
