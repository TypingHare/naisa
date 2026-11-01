package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.SearchVenuesRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueSearchResultResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.math.Stats;
import com.naisa.constant.VenueConstant;
import com.naisa.model.dto.SearchAlternativeVenuesRequest;
import com.naisa.model.dto.SearchAlternativeVenuesRequest.SearchConstraint;
import com.naisa.model.vo.AlternativeVenueResponse;
import com.naisa.util.Beans;
import com.naisa.util.Iters;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

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
     * Searches for open venues for a given group of members within the requested radius and hourly
     * budget.
     *
     * <p>
     * This function first validates the sport ID, date, and time in the given {@code request}.
     * Then, it finds nearby venues that support the given sport and have an hourly price within the
     * requested budget. Finally, it returns those that are open at the requested date and time.
     *
     * @param request search query parameters
     * @return matching venues, or an empty list when none match
     * @throws VenueException if the requested time is in the past or the sport does not exist
     * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
     */
    @Transactional(readOnly = true)
    public List<VenueSearchResultResponse> searchVenues(final SearchVenuesRequest request) {
        final Long sportId = request.getSportId();
        final LocalDateTime requestedAt = LocalDateTime.of(request.getDate(), request.getTime());
        venueService.ensureSportIdExists(sportId);
        venueService.validateSearchRequestTime(requestedAt);

        // Find nearby venues that support the given sport and are within the requested budget.
        final List<EarthCoordinate> memberCoordinates = request.getCoordinates();
        final double maxDistance = request.getMaxDistance();
        final List<VenueSport> affordableVenues = venueService.getAffordableVenuesSupportingSport(
            sportId,
            request.getBudget()
        );
        final List<VenueSearchResultResponse> nearbyVenues = venueService.filterNearbyVenues(
            affordableVenues,
            memberCoordinates,
            maxDistance
        );

        // If no eligible nearby venues are found, return an empty list early.
        if (nearbyVenues.isEmpty()) {
            return List.of();
        }

        // Create a table mapping venue IDs to their interval lists.
        final List<Long> nearbyVenueIds = Iters.map(
            nearbyVenues,
            VenueSearchResultResponse::getVenueId
        );
        final Map<Long, List<OpeningInterval>> intervalsByVenueId = venueService
            .getOpeningIntervalsByVenueId(nearbyVenueIds);

        // Filter out nearby venues that are not open at the requested time.
        final List<VenueSearchResultResponse> openNearbyVenues = new ArrayList<>();
        for (final VenueSearchResultResponse nearbyVenue : nearbyVenues) {
            final List<OpeningInterval> intervals = intervalsByVenueId.getOrDefault(
                nearbyVenue.getVenueId(),
                List.of()
            );
            if (venueService.isOpenAt(intervals, requestedAt)) {
                openNearbyVenues.add(nearbyVenue);
            }
        }

        return openNearbyVenues;
    }

    /**
     * Searches for alternatives by changing only the search values permitted by the client.
     *
     * @param request validated search parameters and permitted changes
     * @return up to 10 alternatives, ordered by fewest changes and closest date and time
     * @throws VenueException if the original time is in the past, the sport is unknown, or no
     *                        offering is within the permitted radius
     * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen, Codex
     */
    @Transactional(readOnly = true)
    public List<AlternativeVenueResponse> searchAlternatives(
        final SearchAlternativeVenuesRequest request
    ) {
        final Long sportId = request.getSportId();
        final LocalDateTime requestedAt = LocalDateTime.of(request.getDate(), request.getTime());
        venueService.ensureSportIdExists(sportId);
        venueService.validateSearchRequestTime(requestedAt);

        final Set<SearchConstraint> allowedChanges = request.getAllowedChanges();
        final double maxDistance = request.getMaxDistance();
        final double extendedMaxDistance = allowedChanges.contains(SearchConstraint.MAX_DISTANCE)
            ? maxDistance + VenueConstant.ALTERNATIVE_DISTANCE_INCREASE_KM
            : maxDistance;
        final BigDecimal budget = request.getBudget();
        final BigDecimal extendedBudget = allowedChanges.contains(SearchConstraint.BUDGET) ? budget
            .add(BigDecimal.valueOf(VenueConstant.ALTERNATIVE_BUDGET_INCREASE)) : budget;

        final List<VenueSport> affordableVenues = venueService.getAffordableVenuesSupportingSport(
            sportId,
            extendedBudget
        );
        final List<VenueSearchResultResponse> nearbyVenues = venueService.filterNearbyVenues(
            affordableVenues,
            request.getCoordinates(),
            extendedMaxDistance
        );
        if (nearbyVenues.isEmpty()) {
            throw VenueException.NO_NEARBY_VENUES.clone();
        }

        // Create a table mapping venue IDs to their interval lists.
        final List<Long> nearbyVenueIds = Iters.map(
            nearbyVenues,
            VenueSearchResultResponse::getVenueId
        );
        final Map<Long, List<OpeningInterval>> intervalsByVenueId = venueService
            .getOpeningIntervalsByVenueId(nearbyVenueIds);

        // We iterate over all nearby venues and check whether each venue is open at each suggested
        // date and time. For each available date and time, we add the venue along with that date
        // and time to `alternativeVenues`.
        final List<LocalDateTime> suggestedDateTimes = suggestDateTimes(request);
        final List<AlternativeVenueResponse> alternativeVenues = new ArrayList<>();
        for (final VenueSearchResultResponse nearbyVenue : nearbyVenues) {
            final List<OpeningInterval> intervals = intervalsByVenueId.getOrDefault(
                nearbyVenue.getVenueId(),
                List.of()
            );
            if (intervals.isEmpty()) {
                continue;
            }

            final double averageDistance = Stats.meanOf(nearbyVenue.getMemberDistancesKm());
            for (final LocalDateTime suggestedAt : suggestedDateTimes) {
                if (!venueService.isOpenAt(intervals, suggestedAt)) {
                    continue;
                }

                final double usedMaxDistance = Math.max(
                    request.getMaxDistance(),
                    Collections.max(nearbyVenue.getMemberDistancesKm())
                );
                final BigDecimal usedBudget = request.getBudget().max(nearbyVenue.getHourlyPrice());
                alternativeVenues.add(Beans.create(AlternativeVenueResponse.class, (it) -> {
                    it.setVenueSearchResult(nearbyVenue);
                    it.setDate(suggestedAt.toLocalDate());
                    it.setTime(suggestedAt.toLocalTime());
                    it.setMaxDistance(usedMaxDistance);
                    it.setAverageDistance(averageDistance);
                    it.setBudget(usedBudget);
                    it.setNumChangedConstraint(countChangedConstraints(request, it));
                    it.setDateTimeDifferenceInMinutes(
                        Math.abs(Duration.between(requestedAt, suggestedAt).toMinutes())
                    );
                }));
            }
        }

        // This comparator first compares the number of changed constraints (fewer is better),
        // then the difference between the requested and alternative date-times (smaller is better),
        // and finally the average distance from the venue to all members' locations (shorter is
        // better). If all these values are equal, the venue ID is used as a tiebreaker.
        final Comparator<AlternativeVenueResponse> comparator = Comparator.comparingInt(
            AlternativeVenueResponse::getNumChangedConstraint
        ).thenComparing(AlternativeVenueResponse::getDateTimeDifferenceInMinutes).thenComparing(
            AlternativeVenueResponse::getAverageDistance
        ).thenComparing(alternative -> alternative.getVenueSearchResult().getVenueId());

        return alternativeVenues.stream().sorted(comparator).limit(
            VenueConstant.ALTERNATIVE_RESULT_LIMIT
        ).toList();
    }

    /**
     * Builds unique future date and time suggestions within the permitted windows.
     *
     * <p>
     * For example, if only the date is allowed to change and the date window is 3 days (see
     * {@link com.naisa.constant.VenueConstant} for the configured value), this function considers:
     *
     * <pre>{@code
     * [(rdate - 3, rtime), (rdate - 2, rtime), ..., (rdate + 3, rtime)]
     * }</pre>
     *
     * where {@code rdate} is the requested date and {@code rtime} is the requested time. The
     * requested date and time are included unless they are already in the past.
     *
     * <p>
     * If both the date and time are allowed to change, and the date and time windows are 3 days and
     * 2 hours, respectively, there are {@code (3 * 2 + 1) * (2 * 2 + 1) = 35} candidate date-time
     * combinations. The returned list may be smaller because suggestions in the past or outside the
     * permitted date range are excluded.
     *
     * @param request original search parameters and permitted changes
     * @return unique suggestions that preserve the original time precision
     * @author Andrew Hsu, Codex, James Chen
     */
    public List<LocalDateTime> suggestDateTimes(final SearchAlternativeVenuesRequest request) {
        final LocalDate requestDate = request.getDate();
        final LocalTime requestTime = request.getTime();

        final LocalDateTime now = LocalDateTime.now(ZoneId.of(VenueConstant.SEARCH_TIMEZONE));
        final Set<SearchConstraint> allowedChanges = request.getAllowedChanges();
        final int dateWindow = allowedChanges.contains(SearchConstraint.DATE)
            ? VenueConstant.ALTERNATIVE_DATE_WINDOW_DAYS
            : 0;
        final int timeWindow = allowedChanges.contains(SearchConstraint.TIME)
            ? VenueConstant.ALTERNATIVE_TIME_WINDOW_HOURS
            : 0;
        final LocalDate earliestDate = requestDate.minusDays(dateWindow);
        final LocalDate latestDate = requestDate.plusDays(dateWindow);

        final Set<LocalDateTime> suggestedDateTimes = new TreeSet<>();
        for (int dayOffset = -dateWindow; dayOffset <= dateWindow; dayOffset++) {
            final LocalDateTime anchor = request.getDate().plusDays(dayOffset).atTime(requestTime);
            for (int hourOffset = -timeWindow; hourOffset <= timeWindow; hourOffset++) {
                final LocalDateTime suggestion = anchor.plusHours(hourOffset);
                if (!suggestion.isBefore(now) && !suggestion.toLocalDate().isBefore(earliestDate)
                    && !suggestion.toLocalDate().isAfter(latestDate)) {
                    suggestedDateTimes.add(suggestion);
                }
            }
        }
        return List.copyOf(suggestedDateTimes);
    }

    /**
     * Counts actual changes from the original search, independent of granted permissions.
     *
     * <p>
     * This function returns an integer representing the number of constraints changed by the given
     * alternative venue. The more changes there are, the less likely the user is to accept the
     * alternative.
     *
     * @param request     original search parameters
     * @param alternative suggested venue and search values
     * @return number of changed date, time, distance, and budget values
     * @author Andrew Hsu, Codex, James Chen
     */
    public int countChangedConstraints(
        final SearchVenuesRequest request,
        final AlternativeVenueResponse alternative
    ) {
        int changes = 0;
        if (!request.getDate().equals(alternative.getDate())) {
            changes++;
        }
        if (!request.getTime().equals(alternative.getTime())) {
            changes++;
        }
        if (Double.compare(request.getMaxDistance(), alternative.getMaxDistance()) != 0) {
            changes++;
        }
        if (request.getBudget().compareTo(alternative.getBudget()) != 0) {
            changes++;
        }
        return changes;
    }
}
