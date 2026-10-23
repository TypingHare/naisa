package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.CompareVenuesRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.CompareVenuesResponse;
import com.naisa.model.vo.CompareVenuesResponse.BestVenues;
import com.naisa.model.vo.CompareVenuesResponse.VenueComparison;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
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
 * Compares venues offering the same sport using location, price, and opening hours.
 *
 * <p>Results are sorted by distance from the user. Crowd status will be included
 * when the venue status service becomes available.
 *
 * @author Emily Wang, Codex
 */
@Service
public class CompareVenuesService {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;
  private final SportRepository sportRepository;
  private final OpeningIntervalRepository openingIntervalRepository;

  /**
   * Creates the service with the repositories required for venue comparison.
   *
   * @author Emily Wang, Codex
   */
  public CompareVenuesService(
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository,
      final SportRepository sportRepository,
      final OpeningIntervalRepository openingIntervalRepository) {
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
    this.sportRepository = sportRepository;
    this.openingIntervalRepository = openingIntervalRepository;
  }

  /**
   * Compares the requested venues and identifies the nearest and cheapest options.
   *
   * @param request validated comparison parameters
   * @return venue comparison results sorted by distance
   * @author Emily Wang, Codex
   */
  @Transactional(readOnly = true)
  public CompareVenuesResponse compareVenues(final CompareVenuesRequest request) {
    final List<Long> venueIds = request.getVenueIds();

    if (new HashSet<>(venueIds).size() != venueIds.size()) {
      throw VenueException.DUPLICATE_IDS.clone();
    }

    if (!sportRepository.existsById(request.getSportId())) {
      throw VenueException.SPORT_NOT_FOUND.clone();
    }

    final List<VenueComparison> comparisons = new ArrayList<>();

    for (final Long venueId : venueIds) {
      final Venue venue =
          venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

      final VenueSport offering =
          venueSportRepository
              .findByVenueIdAndSportId(venueId, request.getSportId())
              .orElseThrow(VenueException.SPORT_NOT_OFFERED::clone);

      final double distanceKm =
          calculateDistance(
              request.getLatitude().doubleValue(),
              request.getLongitude().doubleValue(),
              venue.getLatitude(),
              venue.getLongitude());

      comparisons.add(
          new VenueComparison(
              venueId,
              venue.getName(),
              distanceKm,
              offering.getPrice(),
              offering.getIsIndoor(),
              isOpenNow(venue),
              null));
    }

    comparisons.sort(
        Comparator.comparingDouble(VenueComparison::distanceKm)
            .thenComparing(VenueComparison::venueId));

    final Long nearest = comparisons.get(0).venueId();

    final Long cheapest =
        comparisons.stream()
            .min(
                Comparator.comparing(VenueComparison::hourlyPrice)
                    .thenComparing(VenueComparison::venueId))
            .orElseThrow()
            .venueId();

    return new CompareVenuesResponse(
        comparisons,
        new BestVenues(nearest, cheapest, null));
  }

  private double calculateDistance(
      final double latitude1,
      final double longitude1,
      final double latitude2,
      final double longitude2) {
    final double latitudeDifference = Math.toRadians(latitude2 - latitude1);
    final double longitudeDifference = Math.toRadians(longitude2 - longitude1);

    final double a =
        Math.pow(Math.sin(latitudeDifference / 2), 2)
            + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.pow(Math.sin(longitudeDifference / 2), 2);

    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  private Boolean isOpenNow(final Venue venue) {
    final List<OpeningInterval> intervals =
        openingIntervalRepository.findByVenueId(venue.getId());

    if (intervals.isEmpty() || venue.getTimezone() == null) {
      return null;
    }

    final ZonedDateTime now = ZonedDateTime.now(ZoneId.of(venue.getTimezone()));
    final DayOfWeek today = now.getDayOfWeek();
    final DayOfWeek yesterday = today.minus(1);
    final LocalTime currentTime = now.toLocalTime();

    for (final OpeningInterval interval : intervals) {
      final LocalTime opensAt = interval.getOpensAt();
      final LocalTime closesAt = interval.getClosesAt();
      final boolean overnight = closesAt.isBefore(opensAt);

      if (interval.getDayOfWeek() == today) {
        if (!overnight
            && !currentTime.isBefore(opensAt)
            && currentTime.isBefore(closesAt)) {
          return true;
        }

        if (overnight && !currentTime.isBefore(opensAt)) {
          return true;
        }
      }

      if (interval.getDayOfWeek() == yesterday
          && overnight
          && currentTime.isBefore(closesAt)) {
        return true;
      }
    }

    return false;
  }
}
