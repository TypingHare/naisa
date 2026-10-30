package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueSearchResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles venue search.
 *
 * @author James Chen
 */
@Service
public class VenueSearchService {
  private final VenueService venueService;

  /**
   * Creates a VenueSearchService instance.
   *
   * @param venueService service for basic venue operations
   */
  public VenueSearchService(final VenueService venueService) {
    this.venueService = venueService;
  }

  /**
   * Finds open venues for a given group of members within the requested radius and hourly budget.
   *
   * <p>This function first validates the sport ID, date, and time in the given {@code request}.
   * Then, it finds nearby venues that support the given sport and have an hourly price within the
   * requested budget. Finally, it returns those that are open at the requested date and time.
   *
   * @param request search query parameters
   * @return matching venues, or an empty list when none match
   * @throws VenueException if the requested time is in the past or the sport does not exist
   * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
   */
  @Transactional(readOnly = true)
  public List<VenueSearchResponse> findVenues(final VenueSearchRequest request) {
    final Long sportId = request.getSportId();
    final LocalDateTime requestedAt = LocalDateTime.of(request.getDate(), request.getTime());
    venueService.ensureSportIdExists(sportId);
    venueService.validateSearchRequestTime(requestedAt);

    // Find nearby venues that support the given sport and are within the requested budget.
    final List<EarthCoordinate> memberCoordinates = request.getCoordinates();
    final double maxDistance = request.getMaxDistance().doubleValue();
    final List<VenueSport> affordableVenues =
        venueService.getAffordableVenueSupportingSport(sportId, request.getBudget());
    final List<VenueSearchResponse> nearbyVenues =
        venueService.filterNearbyVenues(affordableVenues, memberCoordinates, maxDistance);

    // If no eligible nearby venues are found, return an empty list early.
    if (nearbyVenues.isEmpty()) {
      return List.of();
    }

    // Create a table mapping venue IDs to their interval lists.
    final List<Long> nearbyVenueIdList =
        nearbyVenues.stream().map(VenueSearchResponse::getVenueId).toList();
    final Map<Long, List<OpeningInterval>> intervalsByVenueId =
        venueService.getIntervalListsByVenueId(nearbyVenueIdList);

    // Filter out nearby venues that are not open at the requested time.
    final List<VenueSearchResponse> openNearbyVenues = new ArrayList<>();
    for (final VenueSearchResponse nearbyVenue : nearbyVenues) {
      final List<OpeningInterval> intervals =
          intervalsByVenueId.getOrDefault(nearbyVenue.getVenueId(), List.of());
      if (venueService.isOpenAt(intervals, requestedAt)) {
        openNearbyVenues.add(nearbyVenue);
      }
    }

    return openNearbyVenues;
  }
}
