package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an opening interval for the venue described by a {@link VenueVo} instance.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class OpeningIntervalVo {
  private DayOfWeek dayOfWeek;
  private LocalTime opensAt;
  private LocalTime closesAt;
}
