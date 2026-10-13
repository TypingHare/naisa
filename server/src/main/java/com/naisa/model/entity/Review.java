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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a review entity.
 *
 * @author James Chen, Vincent Lai
 */
@JavaBean
@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "client_id", nullable = false, comment = "Client that provides the review.")
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

  @NotNull
  @Min(1)
  @Max(5)
  @Column(
      name = "rating",
      nullable = false,
      check =
          @CheckConstraint(name = "reviews_rating_range", constraint = "rating BETWEEN 1 AND 5"))
  private Integer rating;

  @Column(name = "comment", columnDefinition = "text", comment = "Comment written by the end user.")
  private String comment;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
