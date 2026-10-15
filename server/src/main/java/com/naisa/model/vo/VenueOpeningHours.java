package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.beans.JavaBean;
import java.time.LocalTime;

/**
 * Contains one weekly opening period for a venue.
 *
 * @author Andrew Hsu
 */
@JavaBean
public class VenueOpeningHours {
  private String dayOfWeek;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime opensAt;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime closesAt;

  public String getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(final String dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  public LocalTime getOpensAt() {
    return opensAt;
  }

  public void setOpensAt(final LocalTime opensAt) {
    this.opensAt = opensAt;
  }

  public LocalTime getClosesAt() {
    return closesAt;
  }

  public void setClosesAt(final LocalTime closesAt) {
    this.closesAt = closesAt;
  }
}
