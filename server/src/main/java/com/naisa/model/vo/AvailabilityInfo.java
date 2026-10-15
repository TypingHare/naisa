package com.naisa.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.JavaBean;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reports whether a requested time falls within weekly hours. Booking status is unknown.
 *
 * @author Andrew Hsu
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityInfo {
  private Boolean isOpen = true;

  private String bookingStatus = "UNKNOWN";

  private String source = "WEEKLY_OPENING_HOURS";

  private LocalDate date;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime time;

  private String timezone;

  @JsonProperty("isOpen")
  public Boolean isOpen() {
    return isOpen;
  }

  public void setIsOpen(final Boolean isOpen) {
    this.isOpen = isOpen;
  }

  public String getBookingStatus() {
    return bookingStatus;
  }

  public void setBookingStatus(final String bookingStatus) {
    this.bookingStatus = bookingStatus;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(final LocalDate date) {
    this.date = date;
  }

  public LocalTime getTime() {
    return time;
  }

  public void setTime(final LocalTime time) {
    this.time = time;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(final String timezone) {
    this.timezone = timezone;
  }
}
