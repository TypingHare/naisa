package com.naisa.model.vo;

import java.beans.JavaBean;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one weekly opening interval in the venue's local time.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class OpeningIntervalResponse {
  private DayOfWeek dayOfWeek;
  private LocalTime opensAt;
  private LocalTime closesAt;
}
