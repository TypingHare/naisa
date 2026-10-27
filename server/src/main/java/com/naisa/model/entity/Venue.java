package com.naisa.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * Represents a venue entity.
 *
 * <p>A venue is a location where people can play one or more sports. This entity contains the
 * venue's name and location information.
 *
 * <p>The {@code address} field is the venue's USPS address, which serves as its identifier on the
 * map. The {@code latitude} and {@code longitude} fields are used to calculate the distance between
 * users' locations and the venue's location. The {@code timeZone} field is necessary to check
 * whether the venue's opening hours accommodate users' proposed time range.
 *
 * <p>Submitted venues record the authenticated client that created them. Venue submissions do not
 * record an external user identifier. Older or imported venues may have no submission information.
 *
 * @author James Chen, Vincent Lai, Codex
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
  @Size(max = 255, message = "Venue name must not exceed 255 characters.")
  private String name;

  @Column(name = "address", length = 255, nullable = false)
  @Size(max = 255, message = "Address must not exceed 255 characters.")
  private String address;

  @Min(-90)
  @Max(90)
  @Column(name = "latitude", nullable = false)
  private Double latitude;

  @Min(-180)
  @Max(180)
  @Column(name = "longitude", nullable = false)
  private Double longitude;

  @Column(name = "timezone", length = 32, nullable = false)
  @Size(max = 32, message = "Timezone must not exceed 32 characters.")
  private String timezone;

  /** Client service that submitted the venue; absent for unattributed records. */
  @ManyToOne
  @JoinColumn(name = "created_by_client_id", updatable = false)
  private Client createdByClient;

  /** Database-generated creation time; may be unknown for older records. */
  @Generated(event = EventType.INSERT)
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
