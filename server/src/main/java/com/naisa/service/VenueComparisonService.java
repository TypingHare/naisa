package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.VenueComparisonRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueComparisonItem;
import com.naisa.model.vo.VenueComparisonResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.EarthCoordinates;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compares venues offering the same sport using location, price, opening hours, and crowd level.
 *
 * <p>Results are sorted by distance from the user. Crowd levels are computed by {@link
 * CrowdReportService} in the same way as Get Venue Status.
 *
 * @author Emily Wang, Codex, Andrew Hsu, Codex
 */
@Service
public class VenueComparisonService {
  /** Crowd levels from least to most crowded. */
  private static final List<String> CROWD_LEVEL_ORDER =
      List.of("EMPTY", "LIGHT", "MODERATE", "BUSY", "PACKED");

  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;
  private final SportRepository sportRepository;
  private final OpeningIntervalRepository openingIntervalRepository;
  private final CrowdReportService crowdReportService;

  /**
   * Creates the service with the repositories required for venue comparison.
   *
   * @param venueRepository repository for venues
   * @param venueSportRepository repository for sports offered at venues
   * @param sportRepository repository for sports
   * @param openingIntervalRepository repository for venue opening hours
   * @param crowdReportService service for computing crowd levels
   * @author Emily Wang, Codex, Claude Code
   */
  public VenueComparisonService(
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository,
      final SportRepository sportRepository,
      final OpeningIntervalRepository openingIntervalRepository,
      final CrowdReportService crowdReportService) {
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
    this.sportRepository = sportRepository;
    this.openingIntervalRepository = openingIntervalRepository;
    this.crowdReportService = crowdReportService;
  }

  /**
   * Compares the requested venues and identifies the nearest, cheapest, and least crowded ones.
   *
   * <p>Only venues with a known crowd level are considered for the least crowded venue. If no venue
   * has recent crowd reports, the least crowded venue ID is null.
   *
   * @param request validated comparison parameters
   * @return comparison results sorted by distance
   * @throws VenueException if the IDs are duplicated, the sport or a venue does not exist, or a
   *     venue does not offer the sport
   * @author Emily Wang, Codex, Claude Code, Andrew Hsu, Codex, Vincent Lai, Codex
   */
  @Transactional(readOnly = true)
  public VenueComparisonResponse compare(final VenueComparisonRequest request) {
    final List<Long> venueIds = request.getVenueIds();
    if (new HashSet<>(venueIds).size() != venueIds.size()) {
      throw VenueException.DUPLICATE_IDS.clone();
    }

    if (!sportRepository.existsById(request.getSportId())) {
      throw VenueException.SPORT_NOT_FOUND.clone();
    }

    final EarthCoordinate requestCoordinate = EarthCoordinates.createFrom(request);
    final List<VenueComparisonItem> items = new ArrayList<>();
    for (final Long venueId : venueIds) {
      final Venue venue =
          venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

      final VenueSport offering =
          venueSportRepository
              .findByVenueIdAndSportId(venueId, request.getSportId())
              .orElseThrow(VenueException.SPORT_NOT_OFFERED::clone);

      final VenueComparisonItem item = new VenueComparisonItem();
      final EarthCoordinate venueCoordinate = EarthCoordinates.createFrom(venue);

      item.setVenueId(venueId);
      item.setName(venue.getName());
      item.setDistanceKm(EarthCoordinates.calculateDistanceKm(requestCoordinate, venueCoordinate));
      item.setHourlyPrice(offering.getPrice());
      item.setIsIndoor(offering.getIsIndoor());
      item.setOpenNow(isOpenNow(venue));
      item.setCrowdLevel(
          crowdReportService.getCrowdStatus(venueId, request.getSportId()).getCrowdLevel());
      items.add(item);
    }

    items.sort(
        Comparator.comparingDouble(VenueComparisonItem::getDistanceKm)
            .thenComparing(VenueComparisonItem::getVenueId));

    final Long cheapestVenueId =
        items.stream()
            .min(
                Comparator.comparing(VenueComparisonItem::getHourlyPrice)
                    .thenComparing(VenueComparisonItem::getVenueId))
            .orElseThrow()
            .getVenueId();

    final Long leastCrowdedVenueId =
        items.stream()
            .filter(item -> item.getCrowdLevel() != null)
            .min(
                Comparator.comparingInt(
                        (VenueComparisonItem item) ->
                            CROWD_LEVEL_ORDER.indexOf(item.getCrowdLevel()))
                    .thenComparing(VenueComparisonItem::getVenueId))
            .map(VenueComparisonItem::getVenueId)
            .orElse(null);

    final VenueComparisonResponse response = new VenueComparisonResponse();
    response.setVenues(items);
    response.setNearestVenueId(items.get(0).getVenueId());
    response.setCheapestVenueId(cheapestVenueId);
    response.setLeastCrowdedVenueId(leastCrowdedVenueId);
    return response;
  }

  private Boolean isOpenNow(final Venue venue) {
    final List<OpeningInterval> intervals = openingIntervalRepository.findByVenueId(venue.getId());

    if (intervals.isEmpty() || venue.getTimezone() == null) {
      return null;
    }

    return isOpenAt(intervals, ZonedDateTime.now(ZoneId.of(venue.getTimezone())));
  }

  /**
   * Checks whether any opening interval contains the given local date and time.
   *
   * <p>An interval whose closing time is earlier than its opening time is an overnight interval: it
   * starts on its day of the week and ends on the next day. Equal opening and closing times
   * represent 24 hours. Opening times are inclusive and closing times are exclusive. This method
   * takes the time as a parameter so that it can be tested with fixed times.
   *
   * @param intervals opening intervals of a venue
   * @param time local date and time in the venue's time zone
   * @return true if the venue is open at the given time
   * @author Emily Wang, Codex, Claude Code, Andrew Hsu, Codex
   */
  /* default */ static boolean isOpenAt(
      final List<OpeningInterval> intervals, final ZonedDateTime time) {
    final DayOfWeek today = time.getDayOfWeek();
    final DayOfWeek yesterday = today.minus(1);
    final LocalTime currentTime = time.toLocalTime();

    for (final OpeningInterval interval : intervals) {
      final LocalTime opensAt = interval.getOpensAt();
      final LocalTime closesAt = interval.getClosesAt();
      final boolean overnight = !closesAt.isAfter(opensAt);

      if (interval.getDayOfWeek() == today) {
        if (!overnight && !currentTime.isBefore(opensAt) && currentTime.isBefore(closesAt)) {
          return true;
        }

        if (overnight && !currentTime.isBefore(opensAt)) {
          return true;
        }
      }

      if (interval.getDayOfWeek() == yesterday && overnight && currentTime.isBefore(closesAt)) {
        return true;
      }
    }

    return false;
  }
}
