package com.naisa.model.entity;

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
import jakarta.validation.constraints.Size;
import java.beans.JavaBean;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * Represents a review entity.
 *
 * <p>
 * The {@value reviews} table records reviews from end users.
 *
 * <p>
 * The {@code sport} field is optional; records having a missing sport are considered as reviews for
 * the venue.
 *
 * <p>
 * Each report is provided by the end user, ID of whom is provided a validated client. This service
 * doesn't maintain any user data.
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
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@Column(name = "external_user_id", length = 128, nullable = false)
	@Size(max = 128, message = "External user ID must not exceed 128 characters.")
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
	@Column(name = "rating", nullable = false)
	private Short rating;

	@Column(name = "comment_content", columnDefinition = "text")
	private String commentContent;

	@Generated(event = EventType.INSERT)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
