package com.naisa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.CreateVenueRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests submission business rules using mocked repositories, without Spring or a database.
 *
 * <p>AI-generated with Codex. HTTP validation and real persistence are exercised separately by the
 * operations 8-9 Postman collection.
 *
 * @author Vincent Lai, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueSubmissionServiceTest {
  private static final Long CLIENT_ID = 10L;
  private static final Long VENUE_ID = 20L;
  private static final Long SPORT_ID = 30L;
  private static final Long REVIEW_ID = 40L;

  @Mock private VenueRepository venueRepository;
  @Mock private VenueSportRepository venueSportRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private SportRepository sportRepository;
  @Mock private OpeningIntervalRepository openingIntervalRepository;
  @InjectMocks private VenueService venueService;

  private Client client;

  /**
   * Creates the authenticated client supplied by the controller.
   *
   * @author Vincent Lai, Codex
   */
  @BeforeEach
  void setUpClient() {
    client = Beans.create(Client.class, it -> it.setId(CLIENT_ID));
  }

  /**
   * Tests venue creation and rejection before persistence.
   *
   * @author Vincent Lai, Codex
   */
  @Nested
  class AddVenue {
    private CreateVenueRequest request;

    /**
     * Creates a valid request with whitespace to exercise normalization.
     *
     * @author Vincent Lai, Codex
     */
    @BeforeEach
    void setUp() {
      request =
          Beans.create(
              CreateVenueRequest.class,
              it -> {
                it.setName("  Riverside Court  ");
                it.setAddress("  1 Riverside Drive  ");
                it.setLatitude(40.812345);
                it.setLongitude(-73.961234);
                it.setTimezone("  America/New_York  ");
              });
    }

    /**
     * Checks the stored fields, attribution, and database-assigned response identifier.
     *
     * @author Vincent Lai, Codex
     */
    @Test
    void createsNormalizedVenueAttributedToAuthenticatedClient() {
      final Venue saved = Beans.create(Venue.class, it -> it.setId(VENUE_ID));
      when(venueRepository.saveAndFlush(any(Venue.class))).thenReturn(saved);

      assertEquals(VENUE_ID, venueService.addVenue(client, request).getId());

      final ArgumentCaptor<Venue> captor = ArgumentCaptor.forClass(Venue.class);
      verify(venueRepository).saveAndFlush(captor.capture());
      final Venue venue = captor.getValue();
      assertEquals("Riverside Court", venue.getName());
      assertEquals("1 Riverside Drive", venue.getAddress());
      assertEquals(40.812345, venue.getLatitude());
      assertEquals(-73.961234, venue.getLongitude());
      assertEquals("America/New_York", venue.getTimezone());
      assertSame(client, venue.getCreatedByClient());
      assertNull(venue.getId());
      verifyNoInteractions(
          venueSportRepository, reviewRepository, sportRepository, openingIntervalRepository);
    }

    /**
     * Rejects unrecognized and fixed-offset zones without writing anything.
     *
     * @author Vincent Lai, Codex
     */
    @ParameterizedTest
    @ValueSource(strings = {"Mars/Olympus", "america/new_york", "+02:00"})
    void rejectsUnknownTimezone(final String timezone) {
      request.setTimezone(timezone);
      final VenueException exception =
          assertThrows(VenueException.class, () -> venueService.addVenue(client, request));
      assertEquals("venue.invalid_timezone", exception.getCode());
      verifyNoInteractions(
          venueRepository,
          venueSportRepository,
          reviewRepository,
          sportRepository,
          openingIntervalRepository);
    }

    /**
     * Repeating the same payload creates separate venues rather than deduplicating them.
     *
     * @author Vincent Lai, Codex
     */
    @Test
    void repeatedSubmissionsCreateDistinctVenues() {
      when(venueRepository.saveAndFlush(any(Venue.class)))
          .thenAnswer(
              invocation -> {
                final Venue venue = invocation.getArgument(0);
                venue.setId(VENUE_ID);
                return venue;
              });
      venueService.addVenue(client, request);
      venueService.addVenue(client, request);
      final ArgumentCaptor<Venue> captor = ArgumentCaptor.forClass(Venue.class);
      verify(venueRepository, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
      assertNotSame(captor.getAllValues().get(0), captor.getAllValues().get(1));
    }
  }

  /**
   * Tests review creation, updates, scope selection, and business-rule failures.
   *
   * @author Vincent Lai, Codex
   */
  @Nested
  class RateVenue {
    private Venue venue;
    private RateVenueRequest request;

    /**
     * Creates a whole-venue review request with an exact external user identifier.
     *
     * @author Vincent Lai, Codex
     */
    @BeforeEach
    void setUp() {
      venue = Beans.create(Venue.class, it -> it.setId(VENUE_ID));
      request =
          Beans.create(
              RateVenueRequest.class,
              it -> {
                it.setExternalUserId(" user-1 ");
                it.setRating((short) 5);
                it.setComment("  Excellent court.  ");
              });
    }

    /**
     * Creates a review for the venue or its selected sport and locks before querying.
     *
     * @author Vincent Lai, Codex
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = 30)
    void createsReviewForSelectedScope(final Long sportId) {
      request.setSportId(sportId);
      final Sport sport =
          sportId == null ? null : Beans.create(Sport.class, it -> it.setId(sportId));
      when(venueRepository.findByIdForUpdate(VENUE_ID)).thenReturn(Optional.of(venue));
      if (sportId != null) {
        when(venueSportRepository.findByVenueIdAndSportId(VENUE_ID, sportId))
            .thenReturn(Optional.of(Beans.create(VenueSport.class, it -> it.setSport(sport))));
      }
      when(reviewRepository.findExistingReview(CLIENT_ID, " user-1 ", VENUE_ID, sportId))
          .thenReturn(Optional.empty());
      when(reviewRepository.saveAndFlush(any(Review.class)))
          .thenReturn(Beans.create(Review.class, it -> it.setId(REVIEW_ID)));

      assertEquals(REVIEW_ID, venueService.rateVenue(client, VENUE_ID, request).getId());

      final ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
      final org.mockito.InOrder order = inOrder(venueRepository, reviewRepository);
      order.verify(venueRepository).findByIdForUpdate(VENUE_ID);
      order.verify(reviewRepository).findExistingReview(CLIENT_ID, " user-1 ", VENUE_ID, sportId);
      order.verify(reviewRepository).saveAndFlush(captor.capture());
      final Review review = captor.getValue();
      assertSame(client, review.getClient());
      assertSame(venue, review.getVenue());
      assertSame(sport, review.getSport());
      assertEquals(" user-1 ", review.getExternalUserId());
      assertEquals((short) 5, review.getRating());
      assertEquals("Excellent court.", review.getCommentContent());
      assertNull(review.getId());
      if (sportId == null) {
        verifyNoInteractions(venueSportRepository);
      }
    }

    /**
     * Updates in place while preserving the original identity and creation timestamp.
     *
     * @author Vincent Lai, Codex
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void updatesExistingReview(final int rating) {
      final Instant createdAt = Instant.parse("2026-10-20T12:00:00Z");
      final Review existing =
          Beans.create(
              Review.class,
              it -> {
                it.setId(REVIEW_ID);
                it.setCreatedAt(createdAt);
                it.setRating((short) 3);
                it.setCommentContent("Old feedback.");
              });
      request.setRating((short) rating);
      when(venueRepository.findByIdForUpdate(VENUE_ID)).thenReturn(Optional.of(venue));
      when(reviewRepository.findExistingReview(CLIENT_ID, " user-1 ", VENUE_ID, null))
          .thenReturn(Optional.of(existing));
      when(reviewRepository.saveAndFlush(existing)).thenReturn(existing);

      assertEquals(REVIEW_ID, venueService.rateVenue(client, VENUE_ID, request).getId());
      assertEquals((short) rating, existing.getRating());
      assertEquals("Excellent court.", existing.getCommentContent());
      assertEquals(createdAt, existing.getCreatedAt());
      verify(reviewRepository).saveAndFlush(existing);
    }

    /**
     * Includes the current client and exact user ID in the existing-review lookup.
     *
     * @author Vincent Lai, Codex
     */
    @ParameterizedTest
    @ValueSource(strings = {"user-2", " user-1 ", "user-1"})
    void scopesLookupToAuthenticatedClientAndExternalUser(final String externalUserId) {
      final Client otherClient = Beans.create(Client.class, it -> it.setId(99L));
      request.setExternalUserId(externalUserId);
      when(venueRepository.findByIdForUpdate(VENUE_ID)).thenReturn(Optional.of(venue));
      when(reviewRepository.saveAndFlush(any(Review.class)))
          .thenReturn(Beans.create(Review.class, it -> it.setId(REVIEW_ID)));
      venueService.rateVenue(otherClient, VENUE_ID, request);
      verify(reviewRepository).findExistingReview(99L, externalUserId, VENUE_ID, null);
    }

    /**
     * Rejects an absent venue before querying offerings or reviews.
     *
     * @author Vincent Lai, Codex
     */
    @Test
    void rejectsMissingVenue() {
      final VenueException exception =
          assertThrows(
              VenueException.class, () -> venueService.rateVenue(client, VENUE_ID, request));
      assertEquals("venue.not_found", exception.getCode());
      verifyNoInteractions(venueSportRepository, reviewRepository);
    }

    /**
     * Rejects a sport not offered at the venue without persisting a review.
     *
     * @author Vincent Lai, Codex
     */
    @Test
    void rejectsUnsupportedSport() {
      request.setSportId(SPORT_ID);
      when(venueRepository.findByIdForUpdate(VENUE_ID)).thenReturn(Optional.of(venue));
      final VenueException exception =
          assertThrows(
              VenueException.class, () -> venueService.rateVenue(client, VENUE_ID, request));
      assertEquals("venue.sport_not_offered", exception.getCode());
      verifyNoInteractions(reviewRepository);
    }
  }
}
