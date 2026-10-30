package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.AddVenueRequest;
import com.naisa.model.dto.FindAlternativesRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.dto.SearchConstraint;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.FindAlternativeResponse;
import com.naisa.model.vo.ResourceIdResponse;
import com.naisa.model.vo.VenueInfoResponse;
import com.naisa.model.vo.VenueInfoResponse.OpeningIntervalInfo;
import com.naisa.model.vo.VenueInfoResponse.SportInfo;
import com.naisa.model.vo.VenueSearchResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;
import com.naisa.util.EarthCoordinates;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
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
 * Basic venue service.
 *
 * <p>This class includes a collection of basic and reusable service functions that are used by
 * higher-level service classes.
 *
 * @author Vincent Lai, Codex, Andrew Hsu, Codex, James Chen
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
   * Creates a VenueService instance.
   *
   * @param venueRepository repository for venue records
   * @param venueSportRepository repository for sports offered at venues
   * @param reviewRepository repository for user reviews
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
   * Gets affordable venues ({@code VenueSport} instances) that support a given sport.
   *
   * <p>This function retrieves all venues that (1) support the given sport and (2) have an hourly
   * price less than or equal to the given budget.
   *
   * @param sportId the ID of the sport the venue should support
   * @param budget the maximum hourly price for the venue
   * @return a list of affordable venues ({@code VenueSport} instances) that support the sport
   * @author James Chen
   */
  public List<VenueSport> getAffordableVenueSupportingSport(
      final long sportId, final BigDecimal budget) {
    return venueSportRepository.findBySportIdAndPriceLessThanEqual(sportId, budget);
  }

  /**
   * Filters nearby venues.
   *
   * <p>This function checks each given {@code VenueSport} instance to determine whether all members
   * are within the maximum distance of the venue. A venue is considered "nearby" if and only if its
   * distance from every member is less than or equal to {@code maxDistance}.
   *
   * @param venueSports a list of {@code VenueSport} instances to filter
   * @param memberCoordinates the member coordinates
   * @param maxDistance the maximum distance from each member to the venue, in kilometers
   * @return a list of nearby venues that meet the requirements
   * @author James Chen
   */
  public List<VenueSearchResponse> filterNearbyVenues(
      final List<VenueSport> venueSports,
      final Collection<EarthCoordinate> memberCoordinates,
      final double maxDistance) {
    final List<VenueSearchResponse> nearbyVenues = new ArrayList<>();
    for (final VenueSport venueSport : venueSports) {
      final Venue venue = venueSport.getVenue();
      final EarthCoordinate venueCoordinate = EarthCoordinates.createFrom(venue);

      // Calculate the distances to the venue for all members. Skip the venue if any member is
      // farther away than the maximum distance.
      boolean someoneIsTooFarAway = false;
      final List<Double> memberDistancesKm = new ArrayList<>();
      for (final EarthCoordinate memberCoordinate : memberCoordinates) {
        final double distance =
            EarthCoordinates.calculateDistanceKm(memberCoordinate, venueCoordinate);
        if (distance > maxDistance) {
          someoneIsTooFarAway = true;
          break;
        }

        memberDistancesKm.add(distance);
      }
      if (someoneIsTooFarAway) {
        continue;
      }

      nearbyVenues.add(
          Beans.create(
              VenueSearchResponse.class,
              (it) -> {
                it.setVenueId(venue.getId());
                it.setName(venue.getName());
                it.setMemberDistancesKm(memberDistancesKm);
                it.setHourlyPrice(venueSport.getPrice());
                it.setIsIndoor(venueSport.getIsIndoor());
                it.setHasLighting(venueSport.getHasLighting());
              }));
    }

    return nearbyVenues;
  }

  /**
   * Gets a table mapping venue IDs to opening intervals.
   *
   * @param venueIdList a list of venue IDs whose opening intervals to retrieve
   * @return a table mapping venue IDs to opening intervals ({@code OpeningInterval} instances)
   * @author James Chen
   */
  public Map<Long, List<OpeningInterval>> getIntervalListsByVenueId(final List<Long> venueIdList) {
    return openingIntervalRepository.findByVenueIdIn(venueIdList).stream()
        .collect(Collectors.groupingBy(interval -> interval.getVenue().getId()));
  }

  /**
   * Checks whether the requested local date and time fall within the given opening interval.
   *
   * @param interval an opening interval to check
   * @param requestedAt the requested local date and time
   * @return true if the requested time falls within the opening interval; false otherwise
   * @author Andrew Hsu, Codex, James Chen
   */
  public boolean isOpenAt(final OpeningInterval interval, final LocalDateTime requestedAt) {
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
   * Checks whether the requested local date and time fall within any given opening interval.
   *
   * @param intervals a list of opening intervals to check
   * @param requestedAt the requested local date and time
   * @return true if the requested time falls within any opening interval; false otherwise
   * @author James Chen
   */
  public boolean isOpenAt(final List<OpeningInterval> intervals, final LocalDateTime requestedAt) {
    for (final OpeningInterval openingInterval : intervals) {
      if (isOpenAt(openingInterval, requestedAt)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Retrieves a venue's location, offered sports, and weekly opening intervals.
   *
   * <p>Sports are sorted by sport ID. Intervals are sorted by weekday, Monday first, then by
   * opening and closing time. Missing sports or intervals produce empty lists. Hours are returned
   * in the venue's local time, including overnight intervals. Current availability is not checked.
   *
   * @param venueId identifier of the venue
   * @return the venue details with its sports and opening intervals
   * @throws VenueException if the venue does not exist
   * @author Andrew Hsu, Codex, Vincent Lai, Codex
   */
  @Transactional(readOnly = true)
  public VenueInfoResponse getVenueInfo(final Long venueId) {
    final Venue venue =
        venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);
    final VenueInfoResponse response = new VenueInfoResponse();
    response.setVenueId(venue.getId());
    response.setName(venue.getName());
    response.setAddress(venue.getAddress());
    response.setLatitude(venue.getLatitude());
    response.setLongitude(venue.getLongitude());
    response.setTimezone(venue.getTimezone());
    response.setSports(
        venueSportRepository.findByVenueId(venueId).stream()
            .map(
                offering -> {
                  final SportInfo sport = new SportInfo();
                  sport.setSportId(offering.getSport().getId());
                  sport.setName(offering.getSport().getName());
                  sport.setHourlyPrice(offering.getPrice());
                  sport.setIsIndoor(offering.getIsIndoor());
                  return sport;
                })
            .sorted(Comparator.comparing(SportInfo::getSportId))
            .toList());
    response.setOpeningIntervals(
        openingIntervalRepository.findByVenueId(venueId).stream()
            .map(
                interval -> {
                  final OpeningIntervalInfo hours = new OpeningIntervalInfo();
                  hours.setDayOfWeek(interval.getDayOfWeek());
                  hours.setOpensAt(interval.getOpensAt());
                  hours.setClosesAt(interval.getClosesAt());
                  return hours;
                })
            .sorted(
                Comparator.comparing(OpeningIntervalInfo::getDayOfWeek)
                    .thenComparing(OpeningIntervalInfo::getOpensAt)
                    .thenComparing(OpeningIntervalInfo::getClosesAt))
            .toList());
    return response;
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
   * @author Andrew Hsu, Codex, Vincent Lai, Codex
   */
  @Transactional(readOnly = true)
  public List<FindAlternativeResponse> findAlternatives(final FindAlternativesRequest request) {
    // TODO: This function has known issues and will be fixed in the future.
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

    final List<VenueSearchResponse> nearbyVenues = new ArrayList<>();
    final EarthCoordinate coordinate =
        EarthCoordinates.createFrom(request.getCoordinates().getFirst());
    for (final VenueSport offering : venueSportRepository.findBySportId(request.getSportId())) {
      final Venue venue = offering.getVenue();
      final EarthCoordinate venueCoordinate = EarthCoordinates.createFrom(venue);
      final double distanceKm = EarthCoordinates.calculateDistanceKm(coordinate, venueCoordinate);

      if (BigDecimal.valueOf(distanceKm).compareTo(maximumDistance) <= 0) {
        nearbyVenues.add(
            Beans.create(
                VenueSearchResponse.class,
                (it) -> {
                  it.setVenueId(venue.getId());
                  it.setName(venue.getName());
                  it.setHourlyPrice(offering.getPrice());
                  it.setIsIndoor(offering.getIsIndoor());
                  it.setHasLighting(offering.getHasLighting());
                }));
      }
    }
    if (nearbyVenues.isEmpty()) {
      throw VenueException.NO_NEARBY_VENUES.clone();
    }

    final List<VenueSearchResponse> affordableVenues =
        nearbyVenues.stream()
            .filter(venue -> venue.getHourlyPrice().compareTo(maximumBudget) <= 0)
            .toList();
    if (affordableVenues.isEmpty()) {
      return List.of();
    }
    final List<Long> venueIds =
        affordableVenues.stream().map(VenueSearchResponse::getVenueId).toList();
    final Map<Long, List<OpeningInterval>> intervalsByVenue =
        openingIntervalRepository.findByVenueIdIn(venueIds).stream()
            .collect(Collectors.groupingBy(interval -> interval.getVenue().getId()));
    final List<LocalDateTime> suggestedTimes = suggestDateTimes(request, now);
    final List<FindAlternativeResponse> alternatives = new ArrayList<>();
    for (final VenueSearchResponse venue : affordableVenues) {
      final List<OpeningInterval> intervals =
          intervalsByVenue.getOrDefault(venue.getVenueId(), List.of());
      for (final LocalDateTime suggestedAt : suggestedTimes) {
        if (intervals.stream().anyMatch(interval -> isOpenAt(interval, suggestedAt))) {
          alternatives.add(
              new FindAlternativeResponse(
                  venue,
                  suggestedAt.toLocalDate(),
                  suggestedAt.toLocalTime(),
                  request.getMaxDistance(),
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
                .thenComparing(alternative -> alternative.getVenue().getHourlyPrice())
                .thenComparing(alternative -> alternative.getVenue().getVenueId()))
        .limit(ALTERNATIVE_RESULT_LIMIT)
        .toList();
  }

  /**
   * Ensures a sport ID exists.
   *
   * @param sportId the sport ID to check
   * @throws VenueException if the sport ID does not exist
   * @author James Chen
   */
  public void ensureSportIdExists(final Long sportId) {
    if (!sportRepository.existsById(sportId)) {
      throw VenueException.SPORT_NOT_FOUND.clone();
    }
  }

  /**
   * Checks that the requested search time is not in the past.
   *
   * @param requestedAt the search request time to validate
   * @throws VenueException if the search request time is in the past
   * @author James Chen
   */
  public void validateSearchRequestTime(final LocalDateTime requestedAt) {
    final LocalDateTime now = LocalDateTime.now(SEARCH_TIMEZONE);
    if (requestedAt.isBefore(now)) {
      throw VenueException.REQUEST_IN_PAST.clone();
    }
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
      final VenueSearchRequest request, final LocalDateTime now) {
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
      final VenueSearchRequest request, final FindAlternativeResponse alternative) {
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
   * Creates a venue and records the authenticated client who submitted it.
   *
   * <p>The request must already have passed field validation. The timezone is checked against the
   * application's recognized region identifiers before persistence. Coordinates are supplied by the
   * client; this method does not perform address geocoding.
   *
   * <p>Each successful call creates a new venue. Matching names or addresses are not treated as
   * duplicates. Sports, prices, and opening intervals are populated separately.
   *
   * @param client authenticated client submitting the venue
   * @param request validated venue information
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
    venue.setLatitude(request.getLatitude());
    venue.setLongitude(request.getLongitude());
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
    review.setRating(request.getRating());
    review.setCommentContent(request.getComment().strip());

    final Review savedReview = reviewRepository.saveAndFlush(review);
    return new ResourceIdResponse(savedReview.getId());
  }

  /**
   * Gets a sport instance by a venue ID and a sport ID.
   *
   * @param venueId the ID of the venue to search for
   * @param sportId the ID of the sport to search for
   * @return the sport instance that matches both given IDs; result must not be {@code null}
   * @throws VenueException if the sport isn't offered by the venue
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
