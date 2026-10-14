package com.naisa.model.entity;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a venue sport entity.
 *
 * <p>The {@code venue_sports} table records the sports supported by each venue.
 *
 * <p>The {@code price} field is the price of playing the sport at the specified venue. We assume
 * the price is the same every day of the week and remains constant throughout the day. The {@code
 * is_indoor} field indicates whether the sport is played indoors.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity
@Table(
    name = "venue_sports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "sport_id"}))
@Getter
@Setter
public class VenueSport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "venue_id", nullable = false)
  private Venue venue;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sport_id", nullable = false)
  private Sport sport;

  @NotNull
  @DecimalMin("0.00")
  @Digits(integer = 10, fraction = 2)
  @Column(
      name = "price",
      comment = "General price of the sport in the venue in dollars.",
      nullable = false,
      check = @CheckConstraint(name = "venue_sports_price_range", constraint = "price >= 0"))
  private BigDecimal price;

  @Column(name = "is_indoor", comment = "Whether the sport field is indoor.")
  private Boolean isIndoor;
}
