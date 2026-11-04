package com.naisa.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naisa.constant.VenueConstant;
import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.CompareVenuesRequest;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.CompareVenuesResponse;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.model.vo.VenueComparisonResponse;
import com.naisa.repository.SportRepository;
import com.naisa.util.Beans;
import com.naisa.util.EarthCoordinates;

/**
 * Compares venues offering the same sport using location, price, opening hours, and crowd level.
 *
 * <p>
 * Venue details retain request order, with separate ID lists ranked by distance, price, and crowd
 * score. Crowd levels and scores are computed by {@link CrowdReportService}.
 *
 * @author Emily Wang, Codex, Andrew Hsu, Codex, James Chen
 */
@Service
public class VenueComparisonService {
    private final SportRepository sportRepository;
    private final CrowdReportService crowdReportService;
    private final VenueService venueService;

    /**
     * Creates the service with the repository and services required for venue comparison.
     *
     * @param sportRepository    repository for sports
     * @param crowdReportService service for computing crowd levels
     * @param venueService       basic venue service
     * @author Emily Wang, Codex, Claude Code, James Chen
     */
    public VenueComparisonService(
        final SportRepository sportRepository,
        final CrowdReportService crowdReportService,
        final VenueService venueService
    ) {
        this.sportRepository = sportRepository;
        this.crowdReportService = crowdReportService;
        this.venueService = venueService;
    }

    /**
     * Compares the requested venues and ranks their IDs by distance, price, and crowd score.
     *
     * <p>
     * Venue details retain request order. Each ranking is ascending, with ties broken by venue ID.
     * Only venues with a known crowd level appear in the crowd ranking; if none have recent
     * eligible reports, that list is empty. Opening status uses the current system-local time.
     *
     * @param request validated comparison parameters
     * @return venue details and separate ranked ID lists
     * @throws VenueException if the IDs are duplicated, the sport or a venue does not exist, or a
     *                        venue does not offer the sport
     * @author Emily Wang, Codex, Claude Code, Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
     */
    @Transactional(readOnly = true)
    public CompareVenuesResponse compare(final CompareVenuesRequest request) {
        final List<Long> venueIds = request.getVenueIds();
        if (new HashSet<>(venueIds).size() != venueIds.size()) {
            throw VenueException.DUPLICATE_IDS.clone();
        }

        if (!sportRepository.existsById(request.getSportId())) {
            throw VenueException.SPORT_NOT_FOUND.clone();
        }

        final LocalDateTime now = LocalDateTime.now(ZoneId.of(VenueConstant.REQUEST_TIMEZONE));
        final long sportId = request.getSportId();
        final EarthCoordinate requestCoordinate = EarthCoordinates.createFrom(request);
        final List<VenueComparisonResponse> venueComparisonResponses = new ArrayList<>();
        for (final Long venueId : venueIds) {
            venueComparisonResponses.add(
                getVenueComparisonResponse(venueId, sportId, requestCoordinate, now)
            );
        }

        // Rank venue IDs separately by distance, hourly price, and crowd score, lowest first.
        final List<Long> neareastVenueIds = compareVenuesAndGetIds(
            venueComparisonResponses,
            Comparator.comparingDouble(VenueComparisonResponse::getDistanceKm),
            (item) -> true
        );
        final List<Long> cheapestVenueIds = compareVenuesAndGetIds(
            venueComparisonResponses,
            Comparator.comparing(VenueComparisonResponse::getHourlyPrice),
            (item) -> true
        );
        final List<Long> leastCrowdedVenueIds = compareVenuesAndGetIds(
            venueComparisonResponses,
            Comparator.comparingDouble(VenueComparisonResponse::getCrowdLevelScore),
            (item) -> item.getCrowdLevel() != null
        );

        return Beans.create(CompareVenuesResponse.class, (it) -> {
            it.setVenueComparisonResponses(venueComparisonResponses);
            it.setNearestVenueIds(neareastVenueIds);
            it.setCheapestVenueIds(cheapestVenueIds);
            it.setLeastCrowdedVenueIds(leastCrowdedVenueIds);
        });
    }

    /**
     * Builds comparison details for a sport offered at a venue.
     *
     * <p>
     * Opening status is checked at the supplied local time without time-zone conversion. Missing
     * opening hours produce false. Crowd status uses recent reports at the time of the call;
     * {@code now} does not select historical crowd reports.
     *
     * @param venueId           the venue ID
     * @param sportId           the sport ID
     * @param requestCoordinate the user's location for calculating distance
     * @param now               the local date and time used to check opening hours
     * @return distance, price, indoor status, opening status, and crowd status for the venue
     * @throws VenueException if the venue does not exist or does not offer the sport
     *
     * @author James Chen
     */
    public VenueComparisonResponse getVenueComparisonResponse(
        final long venueId,
        final long sportId,
        final EarthCoordinate requestCoordinate,
        final LocalDateTime now
    ) {
        final Venue venue = venueService.getVenue(venueId);
        final VenueSport venueSport = venueService.getVenueSport(venueId, sportId);
        final EarthCoordinate venueCoordinate = EarthCoordinates.createFrom(venue);
        final double distanceKm = EarthCoordinates.calculateDistanceKm(
            requestCoordinate,
            venueCoordinate
        );
        final CrowdStatusResponse crowdStatusResponse = crowdReportService.getCrowdStatus(
            venueId,
            sportId
        );
        return Beans.create(VenueComparisonResponse.class, (it) -> {
            it.setVenueId(venueId);
            it.setName(venue.getName());
            it.setDistanceKm(distanceKm);
            it.setHourlyPrice(venueSport.getPrice());
            it.setIsIndoor(venueSport.getIsIndoor());
            it.setOpenNow(venueService.isVenueOpenAt(venue.getId(), now));
            it.setCrowdLevelScore(crowdStatusResponse.getCrowdLevelScore());
            it.setCrowdLevel(crowdStatusResponse.getCrowdLevel());
        });
    }

    /**
     * Filters comparison results and returns their IDs in comparator order, breaking ties by ID.
     *
     * @param responses  comparison results to rank
     * @param comparator ordering to apply to the retained results
     * @param filter     predicate selecting the results to include
     * @return an unmodifiable list of ranked IDs, empty when no results pass the filter
     * @author James Chen
     */
    private static List<Long> compareVenuesAndGetIds(
        final List<VenueComparisonResponse> responses,
        final Comparator<VenueComparisonResponse> comparator,
        final Predicate<VenueComparisonResponse> filter
    ) {
        return responses.stream().filter(filter).sorted(
            comparator.thenComparing(VenueComparisonResponse::getVenueId)
        ).map(VenueComparisonResponse::getVenueId).toList();
    }
}
