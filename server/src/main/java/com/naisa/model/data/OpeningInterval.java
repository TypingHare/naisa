package com.naisa.model.data;

import java.beans.JavaBean;
import java.time.LocalTime;

/**
 * Contains an opening_hours row for a venue and sport.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
public class OpeningInterval {
  private Integer dayOfWeek;

  private LocalTime opensAt;

  private LocalTime closesAt;

  public Integer getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(final Integer dayOfWeek) {
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
