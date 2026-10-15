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
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * Represents a crowd report entity.
 *
 * <p>The {@code crowd_reports} table stores reports indicating the crowd level at a venue at a
 * specific moment.
 *
 * <p>Each report is submitted by an end user whose ID is provided by a validated client. This
 * service does not maintain any user data.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity
@Table(name = "crowd_reports")
@Getter
@Setter
public class CrowdReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "client_id", nullable = false, comment = "Client that provides the report.")
  private Client client;

  @Column(
      name = "external_user_id",
      length = 128,
      nullable = false,
      comment = "ID of the end user provided by the client.")
  private String externalUserId;

  @ManyToOne
  @JoinColumn(name = "venue_id", nullable = false)
  private Venue venue;

  @ManyToOne
  @JoinColumn(name = "sport_id")
  private Sport sport;

  @Column(
      name = "crowd_level",
      length = 20,
      nullable = false,
      check =
          @CheckConstraint(
              name = "crowd_reports_crowd_level",
              constraint = "crowd_level IN ('EMPTY', 'LIGHT', 'MODERATE', 'BUSY', 'PACKED')"))
  private String crowdLevel;

  @Generated(event = EventType.INSERT)
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
