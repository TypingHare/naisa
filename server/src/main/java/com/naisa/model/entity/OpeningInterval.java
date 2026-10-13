package com.naisa.model.entity;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.beans.JavaBean;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an opening interval entity.
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

  @Column(
      name = "day_of_week",
      nullable = false,
      check =
          @CheckConstraint(
              name = "opening_intervals_day_of_week_enum",
              constraint =
                  "day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', "
                      + "'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')"))
  private String dayOfWeek;

  @Column(name = "opens_at", nullable = false)
  private LocalTime opensAt;

  @Column(name = "closes_at", nullable = false)
  private LocalTime closesAt;
}
