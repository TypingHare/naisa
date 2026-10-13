package com.naisa.model.entity;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a venue entity.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity(name = "venues")
@Getter
@Setter
public class Venue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "name", length = 255, nullable = false)
  @Size(max = 255, message = "Venue name must not exceed 255 characters")
  private String name;

  @Column(name = "address", nullable = false, comment = "USPS address of the venue.")
  private String address;

  @Min(-90)
  @Max(90)
  @Column(
      name = "latitude",
      nullable = false,
      comment = "Latitude of the venue location.",
      check =
          @CheckConstraint(
              name = "venues_latitude_range",
              constraint = "latitude BETWEEN -90 AND 90"))
  private Double latitude;

  @Min(-180)
  @Max(180)
  @Column(
      name = "longitude",
      nullable = false,
      comment = "Longitude of the venue location.",
      check =
          @CheckConstraint(
              name = "venues_longitude_range",
              constraint = "longitude BETWEEN -180 AND 180"))
  private Double longitude;

  @Column(
      name = "timezone",
      length = 32,
      nullable = false,
      comment = "IANA time zone identifier of the venue.")
  private String timezone;
}
