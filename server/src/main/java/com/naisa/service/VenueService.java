package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.AddVenueRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.ResourceIdResponse;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates venue creation and user reviews through table-specific repositories.
 *
 * <p>Callers supply an authenticated client and a request that has passed field validation. The
 * service checks business rules, records client-scoped user identities, and controls transaction
 * boundaries. External user accounts are managed by the calling clients.
 *
 * @author Vincent Lai, Codex
 */
@Service
public class VenueService {
  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;
  private final ReviewRepository reviewRepository;

  /**
   * Creates the service with the repositories required for venue and review persistence.
   *
   * @param venueRepository repository for venue records
   * @param venueSportRepository repository for sports offered at venues
   * @param reviewRepository repository for user ratings and comments
   * @author Vincent Lai, Codex
   */
  public VenueService(
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository,
      final ReviewRepository reviewRepository) {
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
    this.reviewRepository = reviewRepository;
  }

  /**
   * Creates a venue and records the client and external user who submitted it.
   *
   * <p>The request must already have passed field validation. The timezone is checked against the
   * application's recognized region identifiers before persistence. Coordinates are supplied by the
   * client; this method does not perform address geocoding.
   *
   * <p>Each successful call creates a new venue. Matching names or addresses are not treated as
   * duplicates. Sports, prices, and opening intervals are populated separately.
   *
   * @param client authenticated client submitting the venue
   * @param request validated venue information and external user identifier
   * @return the identifier assigned to the created venue
   * @throws VenueException if the timezone is not recognized
   * @author Vincent Lai, Codex
   */
  @Transactional
  public ResourceIdResponse addVenue(
      final Client client, final AddVenueRequest request) {
    final String timezone = request.getTimezone().strip();

    if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
      throw VenueException.INVALID_TIMEZONE.clone();
    }

    final Venue venue = new Venue();
    venue.setName(request.getName().strip());
    venue.setAddress(request.getAddress().strip());
    venue.setLatitude(request.getLatitude().doubleValue());
    venue.setLongitude(request.getLongitude().doubleValue());
    venue.setTimezone(timezone);
    venue.setCreatedByClient(client);
    venue.setCreatedByExternalUserId(request.getExternalUserId());

    final Venue savedVenue = venueRepository.saveAndFlush(venue);
    return new ResourceIdResponse(savedVenue.getId());
  }

  /**
   * Saves a user's rating and comment for a venue or one of its registered sports.
   *
   * <p>A null sport identifier selects the entire venue. Otherwise, the venue must offer the
   * selected sport. If the same client and external user already have a review for the same venue
   * and sport, its rating and comment are replaced while its identifier and creation time remain.
   * Reviews for other sports or clients are independent.
   *
   * <p>The venue row is locked before looking up an existing review. Concurrent submissions using
   * this method therefore serialize per venue, preventing duplicate first submissions. The lock is
   * held until the transaction completes. The database separately enforces review uniqueness.
   *
   * <p>The request must already have passed field validation. External user identifiers are
   * preserved exactly; comments have surrounding whitespace removed.
   *
   * @param client authenticated client submitting the review
   * @param venueId identifier of the venue being reviewed
   * @param request validated rating, comment, external user identifier, and optional sport
   * @return the identifier of the created or updated review
   * @throws VenueException if the venue does not exist or does not offer the requested sport
   * @author Vincent Lai, Codex
   */
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ResourceIdResponse rateVenue(
      final Client client,
      final Long venueId,
      final RateVenueRequest request) {
    final Venue venue =
        venueRepository
            .findByIdForUpdate(venueId)
            .orElseThrow(() -> VenueException.NOT_FOUND.clone());

    final Sport sport;
    if (request.getSportId() == null) {
      sport = null;
    } else {
      final VenueSport offering =
          venueSportRepository
              .findByVenue_IdAndSport_Id(venueId, request.getSportId())
              .orElseThrow(() -> VenueException.SPORT_NOT_OFFERED.clone());
      sport = offering.getSport();
    }

    final Review review =
        reviewRepository
            .findExistingReview(
                client.getId(),
                request.getExternalUserId(),
                venueId,
                request.getSportId())
            .orElseGet(Review::new);

    review.setClient(client);
    review.setExternalUserId(request.getExternalUserId());
    review.setVenue(venue);
    review.setSport(sport);
    review.setRating(request.getRating().shortValueExact());
    review.setCommentContent(request.getComment().strip());

    final Review savedReview = reviewRepository.saveAndFlush(review);
    return new ResourceIdResponse(savedReview.getId());
  }
}