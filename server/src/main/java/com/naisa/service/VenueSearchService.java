package com.naisa.service;

import com.naisa.exception.VenueSearchException;
import com.naisa.model.data.OpeningInterval;
import com.naisa.model.data.VenueCandidate;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.dto.GroupSearchRequest;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.SearchCriteria;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.vo.AvailabilityInfo;
import com.naisa.model.vo.PriceEstimate;
import com.naisa.model.vo.SearchResult;
import com.naisa.model.vo.VenueResult;
import com.naisa.repository.VenueSearchRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Runs proposal Operations 1 and 2 in one search and runs Operation 3 for groups.
 *
 * @author Andrew Hsu
 */
@Service
public final class VenueSearchService {
  private static final int MAX_RELAXATION_STEPS = 3;
  private static final double DISTANCE_STEP_KM = 5;
  private static final BigDecimal BUDGET_STEP = new BigDecimal("5.00");
  private final VenueSearchRepository repository;

  /**
   * Connects the search rules to their data source.
   *
   * @author Andrew Hsu
   */
  public VenueSearchService(final VenueSearchRepository repository) {
    this.repository = repository;
  }

  /**
   * Finds exact venues, then raises the distance and budget limits in bounded steps.
   *
   * @author Andrew Hsu
   */
  public SearchResult findVenues(final VenueSearchRequest request) {
    final VenueSearchRequest original = normalizeSingle(request);
    final List<LocatedVenue> candidates =
        locate(load(original.getSport()), original.getLocation(), null);
    for (int step = 0; step <= MAX_RELAXATION_STEPS; step++) {
      final VenueSearchRequest criteria = normalizeSingle(original);
      criteria.setMaxDistance(
          Math.min(Double.MAX_VALUE, original.getMaxDistance() + step * DISTANCE_STEP_KM));
      criteria.setBudget(original.getBudget().add(BUDGET_STEP.multiply(BigDecimal.valueOf(step))));
      final List<VenueResult> venues = match(candidates, criteria, criteria.getMaxDistance());
      if (!venues.isEmpty()) {
        return response(original, step == 0 ? original : criteria, venues, false);
      }
    }
    return response(original, null, List.of(), true);
  }

  /**
   * Searches around a spherical group center and returns each member's distance.
   *
   * @author Andrew Hsu
   */
  public SearchResult findGroupVenues(final GroupSearchRequest request) {
    final GroupSearchRequest original = new GroupSearchRequest();
    copyCommon(request, original);
    original.setLocations(List.copyOf(request.getLocations()));
    original.setDistanceFromCenter(request.getDistanceFromCenter());
    final Location center = SearchGeometry.center(original.getLocations());
    final List<LocatedVenue> candidates =
        locate(load(original.getSport()), center, original.getLocations());
    final List<VenueResult> venues = match(candidates, original, original.getDistanceFromCenter());
    final SearchResult result =
        response(original, venues.isEmpty() ? null : original, venues, false);
    result.setCenter(center);
    return result;
  }

  /**
   * Distinguishes an unfinished adapter from unknown sports and empty stored data.
   *
   * @author Andrew Hsu
   */
  private List<VenueCandidate> load(final String sport) {
    final VenueSearchData data = repository.findBySportName(sport);
    if (!Boolean.TRUE.equals(data.isImplemented())) {
      throw VenueSearchException.DATA_SOURCE_NOT_IMPLEMENTED.clone();
    }
    if (!Boolean.TRUE.equals(data.isSportFound())) {
      throw VenueSearchException.UNKNOWN_SPORT.clone();
    }
    return data.getVenues();
  }

  /**
   * Computes unrounded distances once for all subsequent requests.
   *
   * @author Andrew Hsu
   */
  private List<LocatedVenue> locate(
      final List<VenueCandidate> data, final Location origin, final List<Location> members) {
    final List<LocatedVenue> candidates = new ArrayList<>();
    for (final VenueCandidate venue : data) {
      if (!validLocation(venue.getLocation())) {
        continue;
      }
      final double distance = SearchGeometry.distance(origin, venue.getLocation());
      final List<Double> memberDistances =
          members == null
              ? List.of()
              : members.stream()
                  .map(member -> SearchGeometry.distance(member, venue.getLocation()))
                  .toList();
      final double average =
          memberDistances.stream().mapToDouble(Double::doubleValue).average().orElse(distance);
      candidates.add(new LocatedVenue(venue, distance, memberDistances, average));
    }
    return candidates;
  }

  /**
   * Applies all checks together and sorts before rounding response values.
   *
   * @author Andrew Hsu
   */
  private List<VenueResult> match(
      final List<LocatedVenue> candidates, final SearchCriteria criteria, final Double radius) {
    return candidates.stream()
        .filter(candidate -> radius == null || candidate.distance() <= radius)
        .filter(candidate -> comparablePrice(candidate.venue()))
        .filter(candidate -> candidate.venue().getPrice().compareTo(criteria.getBudget()) <= 0)
        .filter(candidate -> isOpen(candidate.venue(), criteria))
        .sorted(
            Comparator.comparingDouble(LocatedVenue::average)
                .thenComparing(candidate -> candidate.venue().getPrice())
                .thenComparing(candidate -> candidate.venue().getVenueId()))
        .map(candidate -> venueResult(candidate, criteria))
        .toList();
  }

  /**
   * Excludes unknown prices and incompatible units rather than treating them as free.
   *
   * @author Andrew Hsu
   */
  private boolean comparablePrice(final VenueCandidate venue) {
    return venue.getPrice() != null
        && venue.getPrice().signum() >= 0
        && "PER_COURT_HOUR".equals(venue.getPriceUnit());
  }

  /**
   * Checks local weekly hours, including the preceding day's overnight interval.
   *
   * @author Andrew Hsu
   */
  private boolean isOpen(final VenueCandidate venue, final SearchCriteria criteria) {
    if (venue.getTimezone() == null) {
      return false;
    }
    try {
      final ZoneId zone = ZoneId.of(venue.getTimezone());
      if (zone.getRules()
          .getValidOffsets(criteria.getDate().atTime(criteria.getTime()))
          .isEmpty()) {
        return false;
      }
    } catch (DateTimeException exception) {
      return false;
    }
    if (venue.getOpeningHours() == null) {
      return false;
    }
    final int weekday = criteria.getDate().getDayOfWeek().getValue();
    final int previousWeekday = weekday == 1 ? 7 : weekday - 1;
    for (final OpeningInterval hours : venue.getOpeningHours()) {
      if (hours == null
          || hours.getDayOfWeek() == null
          || hours.getOpensAt() == null
          || hours.getClosesAt() == null) {
        continue;
      }
      final LocalTime opens = hours.getOpensAt();
      final LocalTime closes = hours.getClosesAt();
      final LocalTime time = criteria.getTime();
      if (opens.isBefore(closes)
          && hours.getDayOfWeek() == weekday
          && !time.isBefore(opens)
          && time.isBefore(closes)) {
        return true;
      }
      if (closes.isBefore(opens)
          && ((hours.getDayOfWeek() == weekday && !time.isBefore(opens))
              || (hours.getDayOfWeek() == previousWeekday && time.isBefore(closes)))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Rejects unusable stored coordinates without performing distance arithmetic on them.
   *
   * @author Andrew Hsu
   */
  private boolean validLocation(final Location location) {
    return location != null
        && location.getLatitude() != null
        && location.getLongitude() != null
        && Double.isFinite(location.getLatitude())
        && Double.isFinite(location.getLongitude())
        && Math.abs(location.getLatitude()) <= 90
        && Math.abs(location.getLongitude()) <= 180;
  }

  /**
   * Builds response fields without exposing persistence objects.
   *
   * @author Andrew Hsu
   */
  private VenueResult venueResult(final LocatedVenue candidate, final SearchCriteria criteria) {
    final VenueCandidate venue = candidate.venue();
    final VenueResult result = new VenueResult();
    result.setVenueId(venue.getVenueId().toString());
    result.setVenueName(venue.getVenueName());
    result.setSport(venue.getSport());
    if (candidate.memberDistances().isEmpty()) {
      result.setDistanceKm(rounded(candidate.distance()));
    } else {
      result.setDistanceFromCenterKm(rounded(candidate.distance()));
      result.setAverageDistanceKm(rounded(candidate.average()));
      result.setMemberDistancesKm(candidate.memberDistances().stream().map(this::rounded).toList());
    }
    final PriceEstimate price = new PriceEstimate();
    price.setAmount(venue.getPrice());
    result.setEstimatedPrice(price);
    result.setFacility(venue.getFacility());
    final AvailabilityInfo availability = new AvailabilityInfo();
    availability.setDate(criteria.getDate());
    availability.setTime(criteria.getTime());
    availability.setTimezone(venue.getTimezone());
    result.setAvailability(availability);
    return result;
  }

  /**
   * Rounds only presentation values to three decimal places.
   *
   * @author Andrew Hsu
   */
  private double rounded(final double value) {
    return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Keeps original criteria separate from the criteria met by returned alternatives.
   *
   * @author Andrew Hsu
   */
  private SearchResult response(
      final SearchCriteria original,
      final SearchCriteria effective,
      final List<VenueResult> venues,
      final boolean limitReached) {
    final SearchResult result = new SearchResult();
    result.setRequestedCriteria(original);
    result.setEffectiveCriteria(effective);
    result.setVenues(venues);
    result.setSearchLimitReached(limitReached);
    result.setMatchType(
        effective == null ? "NONE" : effective == original ? "EXACT" : "ALTERNATIVE");
    if (original instanceof VenueSearchRequest requested
        && effective instanceof VenueSearchRequest matched) {
      result.setRelaxedFields(changedFields(requested, matched));
    }
    return result;
  }

  /**
   * Lists actual changes in a fixed order.
   *
   * @author Andrew Hsu
   */
  private List<String> changedFields(
      final VenueSearchRequest original, final VenueSearchRequest alternative) {
    final List<String> changed = new ArrayList<>();
    if (!Objects.equals(original.getDate(), alternative.getDate())) {
      changed.add("date");
    }
    if (!Objects.equals(original.getTime(), alternative.getTime())) {
      changed.add("time");
    }
    if (!Objects.equals(original.getMaxDistance(), alternative.getMaxDistance())) {
      changed.add("maxDistance");
    }
    if (original.getBudget().compareTo(alternative.getBudget()) != 0) {
      changed.add("budget");
    }
    return changed;
  }

  /**
   * Copies the single-location criteria without changing the input.
   *
   * @author Andrew Hsu
   */
  private VenueSearchRequest normalizeSingle(final VenueSearchRequest request) {
    final VenueSearchRequest normalized = new VenueSearchRequest();
    copyCommon(request, normalized);
    normalized.setLocation(request.getLocation());
    normalized.setMaxDistance(request.getMaxDistance());
    return normalized;
  }

  /**
   * Normalizes common fields without changing the caller's object.
   *
   * @author Andrew Hsu
   */
  private void copyCommon(final SearchCriteria source, final SearchCriteria destination) {
    destination.setSport(source.getSport().trim());
    destination.setDate(source.getDate());
    destination.setTime(source.getTime());
    destination.setBudget(source.getBudget());
  }

  /**
   * Keeps unrounded values for filters and ordering.
   *
   * @author Andrew Hsu
   */
  private record LocatedVenue(
      VenueCandidate venue, double distance, List<Double> memberDistances, double average) {}
}
