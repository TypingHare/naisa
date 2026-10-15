package com.naisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.beans.JavaBean;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an opening interval entity.
 *
 * <p>The {@code opening_intervals} table records the time intervals during which a venue is open.
 * We allow a venue to have multiple opening intervals on the same day of the week. However, we
 * assume that all sports at a given venue share the same opening intervals.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity
@Table(name = "opening_intervals")
@Getter
@Setter
public class OpeningInterval {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "venue_id", nullable = false)
  private Venue venue;

  @Column(name = "day_of_week", length = 9, nullable = false)
  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek;

  @Column(name = "opens_at", nullable = false)
  private LocalTime opensAt;

  @Column(name = "closes_at", nullable = false)
  private LocalTime closesAt;
}
