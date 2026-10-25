package com.naisa.service;

import com.naisa.common.VenueDistance;
import com.naisa.exception.VenueException;
import com.naisa.model.dto.AddVenueRequest;
import com.naisa.model.dto.FindAlternativesRequest;
import com.naisa.model.dto.FindVenuesRequest;
import com.naisa.model.dto.GroupMemberLocation;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.dto.SearchConstraint;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.FindAlternativeResponse;
import com.naisa.model.vo.FindVenueResponse;
import com.naisa.model.vo.ResourceIdResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates venue searches, creation, and user reviews through table-specific repositories.
 *
 * <p>Callers supply an authenticated client and a request that has passed field validation. The
 * service checks business rules, records client-scoped user identities, and controls transaction
 * boundaries. External user accounts are managed by the calling clients.
 *
 * @author Vincent Lai, Codex, Andrew Hsu, Codex
 */
@Service
public class VenueService {
  private static final ZoneId SEARCH_TIMEZONE = ZoneId.of("America/New_York");
  private static final int ALTERNATIVE_DATE_WINDOW_DAYS = 3;
  private static final int ALTERNATIVE_TIME_WINDOW_HOURS = 2;
  private static final BigDecimal ALTERNATIVE_DISTANCE_INCREASE_KM = BigDecimal.valueOf(5);
  private static final BigDecimal ALTERNATIVE_BUDGET_INCREASE = BigDecimal.valueOf(20);
  private static final int ALTERNATIVE_RESULT_LIMIT = 10;

  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;
  private final ReviewRepository reviewRepository;
  private final SportRepository sportRepository;
  private final OpeningIntervalRepository openingIntervalRepository;

  /**
   * Creates the service with the repositories required for venue searches and submissions.
   *
   * @param venueRepository repository for venue records
   * @param venueSportRepository repository for sports offered at venues
   * @param reviewRepository repository for user ratings and comments
   * @param sportRepository repository for registered sports
   * @param openingIntervalRepository repository for venue opening hours
   * @author Vincent Lai, Codex, Andrew Hsu, Codex
   */
  public VenueService(
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository,
      final ReviewRepository reviewRepository,
      final SportRepository sportRepository,
      final OpeningIntervalRepository openingIntervalRepository) {
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
    this.reviewRepository = reviewRepository;
    this.sportRepository = sportRepository;
    this.openingIntervalRepository = openingIntervalRepository;
  }

  /**
   * Finds open venues for one origin or a group within the requested radius and hourly budget.
   *
   * <p>The request must have passed field validation. The center is the average latitude and
   * longitude. The radius limits distance from this center, not from each member. Member distances
   * follow input order. Results are sorted by average member distance, then venue identifier.
   * A single origin uses the same calculation and filters as a group.
   *
   * @param request validated search query parameters
   * @return matching venues, or an empty list when none match
   * @throws VenueException if the requested time is in the past or the sport does not exist
   * @author Andrew Hsu, Codex
   */
  @Transactional(readOnly = true)
  public List<FindVenueResponse> findVenues(final FindVenuesRequest request) {
    final LocalDateTime requestedAt =
        validateSearchRequest(request, LocalDateTime.now(SEARCH_TIMEZONE));

    final List<GroupMemberLocation> locations = searchLocations(request);
    final double centerLatitude =
        locations.stream().mapToDouble(location -> location.getLatitude().doubleValue())
            .average().orElseThrow();
    final double centerLongitude =
        locations.stream().mapToDouble(location -> location.getLongitude().doubleValue())
            .average().orElseThrow();
    final List<VenueSport> offerings = venueSportRepository.findBySportIdAndPriceLessThanEqual(
        request.getSportId(), request.getBudget());
    final List<FindVenueResponse> nearbyVenues = new ArrayList<>();
    for (final VenueSport offering : offerings) {
      final Venue venue = offering.getVenue();
      final double distanceKm = VenueDistance.calculateDistanceKm(
          centerLatitude, centerLongitude, venue.getLatitude(), venue.getLongitude());
      if (BigDecimal.valueOf(distanceKm).compareTo(request.getMaxDistance()) <= 0) {
        final List<Double> memberDistances = locations.stream()
            .map(location -> VenueDistance.calculateDistanceKm(
                location.getLatitude().doubleValue(), location.getLongitude().doubleValue(),
                venue.getLatitude(), venue.getLongitude()))
            .toList();
        final FindVenueResponse result = new FindVenueResponse(
            venue.getId(), venue.getName(), distanceKm, offering.getPrice(),
            offering.getIsIndoor(), true);
        result.setMemberDistancesKm(memberDistances);
        result.setAverageDistanceKm(
            memberDistances.stream().mapToDouble(Double::doubleValue).average().orElseThrow());
        nearbyVenues.add(result);
      }
    }
    if (nearbyVenues.isEmpty()) {
      return List.of();
    }

    final List<Long> venueIds = nearbyVenues.stream().map(FindVenueResponse::getVenueId).toList();
    final Map<Long, List<OpeningInterval>> intervalsByVenue =
        openingIntervalRepository.findByVenueIdIn(venueIds).stream()
            .collect(Collectors.groupingBy(interval -> interval.getVenue().getId()));

    return nearbyVenues.stream()
        .filter(venue -> intervalsByVenue.getOrDefault(venue.getVenueId(), List.of()).stream()
            .anyMatch(interval -> isOpenAt(interval, requestedAt)))
        .sorted(Comparator.comparingDouble(FindVenueResponse::getAverageDistanceKm)
            .thenComparing(FindVenueResponse::getVenueId))
        .toList();
  }

  /**
   * Converts a single coordinate pair to a one-member list, or uses the supplied member list.
   *
   * @param request search parameters that have passed field validation
   * @return locations in member order
   * @author Andrew Hsu, Codex
   */
  private static List<GroupMemberLocation> searchLocations(final FindVenuesRequest request) {
    if (request.getLocations() != null) {
      return request.getLocations();
    }
    final GroupMemberLocation location = new GroupMemberLocation();
    location.setLatitude(request.getLatitude());
    location.setLongitude(request.getLongitude());
    return List.of(location);
  }
 
  /** 
   * Finds alternatives by changing only the search values permitted by the client.
   *
   * <p>The request must already have passed field validation. A nearby sport offering is checked
   * before budget or opening hours, so an unavailable alternative differs from no nearby venue.
   *
   * @param request validated search parameters and permitted changes
   * @return up to 10 alternatives, ordered by fewest changes and closest date and time
   * @throws VenueException if the original time is in the past, the sport is unknown, or no
   *     offering is within the permitted radius
   * @author Andrew Hsu, Codex
   */
  @Transactional(readOnly = true)
  public List<FindAlternativeResponse> findAlternatives(final FindAlternativesRequest request) {
    final LocalDateTime now = LocalDateTime.now(SEARCH_TIMEZONE);
    final LocalDateTime requestedAt = validateSearchRequest(request, now);
    final Set<SearchConstraint> allowedChanges = request.getAllowedChanges();
    final BigDecimal maximumDistance =
        allowedChanges.contains(SearchConstraint.MAX_DISTANCE)
            ? request.getMaxDistance().add(ALTERNATIVE_DISTANCE_INCREASE_KM)
            : request.getMaxDistance();
    final BigDecimal maximumBudget =
        allowedChanges.contains(SearchConstraint.BUDGET)
            ? request.getBudget().add(ALTERNATIVE_BUDGET_INCREASE)
            : request.getBudget();

    final List<FindVenueResponse> nearbyVenues = new ArrayList<>();
    for (final VenueSport offering : venueSportRepository.findBySportId(request.getSportId())) {
      final Venue venue = offering.getVenue();
      final double distanceKm =
          VenueDistance.calculateDistanceKm(
              request.getLatitude().doubleValue(),
              request.getLongitude().doubleValue(),
              venue.getLatitude(),
              venue.getLongitude());
      if (BigDecimal.valueOf(distanceKm).compareTo(maximumDistance) <= 0) {
        nearbyVenues.add(
            new FindVenueResponse(
                venue.getId(),
                venue.getName(),
                distanceKm,
                offering.getPrice(),
                offering.getIsIndoor(),
                true));
      }
    }
    if (nearbyVenues.isEmpty()) {
      throw VenueException.NO_NEARBY_VENUES.clone();
    }

    final List<FindVenueResponse> affordableVenues =
        nearbyVenues.stream()
            .filter(venue -> venue.getHourlyPrice().compareTo(maximumBudget) <= 0)
            .toList();
    if (affordableVenues.isEmpty()) {
      return List.of();
    }
    final List<Long> venueIds =
        affordableVenues.stream().map(FindVenueResponse::getVenueId).toList();
    final Map<Long, List<OpeningInterval>> intervalsByVenue =
        openingIntervalRepository.findByVenueIdIn(venueIds).stream()
            .collect(Collectors.groupingBy(interval -> interval.getVenue().getId()));
    final List<LocalDateTime> suggestedTimes = suggestDateTimes(request, now);
    final List<FindAlternativeResponse> alternatives = new ArrayList<>();
    for (final FindVenueResponse venue : affordableVenues) {
      final List<OpeningInterval> intervals =
          intervalsByVenue.getOrDefault(venue.getVenueId(), List.of());
      for (final LocalDateTime suggestedAt : suggestedTimes) {
        if (intervals.stream().anyMatch(interval -> isOpenAt(interval, suggestedAt))) {
          alternatives.add(
              new FindAlternativeResponse(
                  venue,
                  suggestedAt.toLocalDate(),
                  suggestedAt.toLocalTime(),
                  request.getMaxDistance().max(BigDecimal.valueOf(venue.getDistanceKm())),
                  request.getBudget().max(venue.getHourlyPrice())));
        }
      }
    }

    return alternatives.stream()
        .sorted(
            Comparator.comparingInt(
                    (FindAlternativeResponse alternative) ->
                        countChangedConstraints(request, alternative))
                .thenComparing(
                    alternative ->
                        Duration.between(
                                requestedAt,
                                LocalDateTime.of(alternative.getDate(), alternative.getTime()))
                            .abs())
                .thenComparing(FindAlternativeResponse::getDate)
                .thenComparing(FindAlternativeResponse::getTime)
                .thenComparingDouble(alternative -> alternative.getVenue().getDistanceKm())
                .thenComparing(alternative -> alternative.getVenue().getHourlyPrice())
                .thenComparing(alternative -> alternative.getVenue().getVenueId()))
        .limit(ALTERNATIVE_RESULT_LIMIT)
        .toList();
  }

  /**
   * Checks the original search time and sport for either search operation.
   *
   * @param request search parameters that have passed field validation
   * @param now current local date and time in New York
   * @return the original requested date and time
   * @throws VenueException if the time is in the past or the sport is unknown
   * @author Andrew Hsu, Codex
   */
  private LocalDateTime validateSearchRequest(
      final FindVenuesRequest request, final LocalDateTime now) {
    final LocalDateTime requestedAt = LocalDateTime.of(request.getDate(), request.getTime());
    if (requestedAt.isBefore(now)) {
      throw VenueException.REQUEST_IN_PAST.clone();
    }
    if (!sportRepository.existsById(request.getSportId())) {
      throw VenueException.SPORT_NOT_FOUND.clone();
    }
    return requestedAt;
  }

  /**
   * Builds unique future date and time suggestions within the permitted windows.
   *
   * @param request original search parameters and permitted changes
   * @param now current local date and time in New York
   * @return unique suggestions that preserve the original time precision
   * @author Andrew Hsu, Codex
   */
  private static List<LocalDateTime> suggestDateTimes(
      final FindAlternativesRequest request, final LocalDateTime now) {
    final int dateWindow =
        request.getAllowedChanges().contains(SearchConstraint.DATE)
            ? ALTERNATIVE_DATE_WINDOW_DAYS
            : 0;
    final int timeWindow =
        request.getAllowedChanges().contains(SearchConstraint.TIME)
            ? ALTERNATIVE_TIME_WINDOW_HOURS
            : 0;
    final LocalDate earliestDate = request.getDate().minusDays(dateWindow);
    final LocalDate latestDate = request.getDate().plusDays(dateWindow);
    final Set<LocalDateTime> suggestions = new TreeSet<>();
    for (int dayOffset = -dateWindow; dayOffset <= dateWindow; dayOffset++) {
      final LocalDateTime anchor = request.getDate().plusDays(dayOffset).atTime(request.getTime());
      for (int hourOffset = -timeWindow; hourOffset <= timeWindow; hourOffset++) {
        final LocalDateTime suggestion = anchor.plusHours(hourOffset);
        if (!suggestion.isBefore(now)
            && !suggestion.toLocalDate().isBefore(earliestDate)
            && !suggestion.toLocalDate().isAfter(latestDate)) {
          suggestions.add(suggestion);
        }
      }
    }
    return List.copyOf(suggestions);
  }

  /**
   * Counts actual changes from the original search, independent of granted permissions.
   *
   * @param request original search parameters
   * @param alternative suggested venue and search values
   * @return number of changed date, time, distance, and budget values
   * @author Andrew Hsu, Codex
   */
  private static int countChangedConstraints(
      final FindVenuesRequest request, final FindAlternativeResponse alternative) {
    int changes = 0;
    if (!request.getDate().equals(alternative.getDate())) {
      changes++;
    }
    if (!request.getTime().equals(alternative.getTime())) {
      changes++;
    }
    if (request.getMaxDistance().compareTo(alternative.getMaxDistance()) != 0) {
      changes++;
    }
    if (request.getBudget().compareTo(alternative.getBudget()) != 0) {
      changes++;
    }
    return changes;
  }

  /**
   * Checks whether a weekly opening interval contains the requested local date and time.
   *
   * @param interval stored opening interval
   * @param requestedAt requested date and time in New York
   * @return true from opening time up to, but excluding, closing time
   * @author Andrew Hsu, Codex
   */
  private static boolean isOpenAt(final OpeningInterval interval, final LocalDateTime requestedAt) {
    final int daysSinceOpening =
        Math.floorMod(
            requestedAt.getDayOfWeek().getValue() - interval.getDayOfWeek().getValue(), 7);
    final LocalDateTime opensAt =
        requestedAt.toLocalDate().minusDays(daysSinceOpening).atTime(interval.getOpensAt());
    LocalDateTime closesAt = opensAt.toLocalDate().atTime(interval.getClosesAt());
    if (!interval.getClosesAt().isAfter(interval.getOpensAt())) {
      closesAt = closesAt.plusDays(1);
    }
    return !requestedAt.isBefore(opensAt) && requestedAt.isBefore(closesAt);
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
  public ResourceIdResponse addVenue(final Client client, final AddVenueRequest request) {
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
      final Client client, final Long venueId, final RateVenueRequest request) {
    final Venue venue =
        venueRepository.findByIdForUpdate(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

    final Sport sport;
    if (request.getSportId() == null) {
      sport = null;
    } else {
      final VenueSport offering =
          venueSportRepository
              .findByVenueIdAndSportId(venueId, request.getSportId())
              .orElseThrow(VenueException.SPORT_NOT_OFFERED::clone);
      sport = offering.getSport();
    }

    final Review review =
        reviewRepository
            .findExistingReview(
                client.getId(), request.getExternalUserId(), venueId, request.getSportId())
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

  /**
   * Gets a sport instance by a venue ID and a sport ID.
   *
   * <p>If the sport isn't offered by the Venue, it throws {@code VenueException.SPORT_NOT_OFFERED}.
   *
   * @param venueId the ID of the venue to search for
   * @param sportId the ID of the sport to search for
   * @return the sport instance that matches both given IDs; result must not be {@code null}
   * @author James Chen.
   */
  public Sport getVenueSport(final Long venueId, final Long sportId) {
    final VenueSport venueSport =
        venueSportRepository
            .findByVenueIdAndSportId(venueId, sportId)
            .orElseThrow(VenueException.SPORT_NOT_OFFERED::clone);
    return venueSport.getSport();
  }
}
