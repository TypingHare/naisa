package com.naisa.service;

import com.naisa.constant.VenueConstant;
import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.CreateVenueRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.OpeningIntervalResponse;
import com.naisa.model.vo.VenueSearchResultResponse;
import com.naisa.model.vo.VenueSportResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;
import com.naisa.util.EarthCoordinates;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic venue service.
 *
 * <p>
 * This class includes a collection of basic and reusable service functions that are used by
 * higher-level service classes.
 *
 * @author Vincent Lai, Codex, Andrew Hsu, Codex, James Chen
 */
@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueSportRepository venueSportRepository;
    private final ReviewRepository reviewRepository;
    private final SportRepository sportRepository;
    private final OpeningIntervalRepository openingIntervalRepository;

    /**
     * Creates a VenueService instance.
     *
     * @param venueRepository           repository for venue records
     * @param venueSportRepository      repository for sports offered at venues
     * @param reviewRepository          repository for user reviews
     * @param sportRepository           repository for registered sports
     * @param openingIntervalRepository repository for venue opening hours
     * @author Vincent Lai, Codex, Andrew Hsu, Codex
     */
    public VenueService(
        final VenueRepository venueRepository,
        final VenueSportRepository venueSportRepository,
        final ReviewRepository reviewRepository,
        final SportRepository sportRepository,
        final OpeningIntervalRepository openingIntervalRepository
    ) {
        this.venueRepository = venueRepository;
        this.venueSportRepository = venueSportRepository;
        this.reviewRepository = reviewRepository;
        this.sportRepository = sportRepository;
        this.openingIntervalRepository = openingIntervalRepository;
    }

    /**
     * Retrieves a venue by its ID.
     *
     * @param venueId the ID of the venue to retrieve
     * @return the venue associated with the given ID
     * @throws VenueException if the venue does not exist
     * @author James Chen
     */
    public Venue getVenue(final Long venueId) {
        return venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);
    }

    /**
     * Retrieves all sports ({@code SportVo} instances) that a specific venue supports.
     *
     * @param venueId the ID of the venue
     * @return a list of sports ({@code SportVo} instances) supported by the venue
     * @author James Chen
     */
    public List<VenueSportResponse> getSportVos(final Long venueId) {
        final List<VenueSport> venueSports = venueSportRepository.findByVenueId(venueId);
        final List<VenueSportResponse> sports = new ArrayList<>();
        for (final VenueSport venueSport : venueSports) {
            sports.add(Beans.create(VenueSportResponse.class, (it) -> {
                it.setSportId(venueSport.getSport().getId());
                it.setName(venueSport.getSport().getName());
                it.setHourlyPrice(venueSport.getPrice());
                it.setIsIndoor(venueSport.getIsIndoor());
                it.setHasLighting(venueSport.getHasLighting());
            }));
        }

        return sports;
    }

    /**
     * Retrieves all opening intervals associated with a specified venue.
     *
     * @param venueId the ID of the venue
     * @return a list of opening intervals ({@code OpeningIntervalVo} instances) associated with the
     *         venue
     * @author James Chen
     */
    public List<OpeningIntervalResponse> getOpeningIntervalVos(final Long venueId) {
        final List<OpeningIntervalResponse> openingIntervalVos = new ArrayList<>();
        final List<OpeningInterval> openingIntervals = openingIntervalRepository.findByVenueId(
            venueId
        );
        for (final OpeningInterval openingInterval : openingIntervals) {
            openingIntervalVos.add(Beans.create(OpeningIntervalResponse.class, (it) -> {
                it.setDayOfWeek(openingInterval.getDayOfWeek());
                it.setOpensAt(openingInterval.getOpensAt());
                it.setClosesAt(openingInterval.getClosesAt());
            }));
        }

        return openingIntervalVos;
    }

    /**
     * Gets affordable venues ({@code VenueSport} instances) that support a given sport.
     *
     * <p>
     * This function retrieves all venues that (1) support the given sport and (2) have an hourly
     * price less than or equal to the given budget.
     *
     * @param sportId the ID of the sport the venue should support
     * @param budget  the maximum hourly price for the venue
     * @return a list of affordable venues ({@code VenueSport} instances) that support the sport
     * @author James Chen
     */
    public List<VenueSport> getAffordableVenueSupportingSport(
        final long sportId,
        final BigDecimal budget
    ) {
        return venueSportRepository.findBySportIdAndPriceLessThanEqual(sportId, budget);
    }

    /**
     * Filters nearby venues.
     *
     * <p>
     * This function checks each given {@code VenueSport} instance to determine whether all members
     * are within the maximum distance of the venue. A venue is considered "nearby" if and only if
     * its distance from every member is less than or equal to {@code maxDistance}.
     *
     * @param venueSports       a list of {@code VenueSport} instances to filter
     * @param memberCoordinates the member coordinates
     * @param maxDistance       the maximum distance from each member to the venue, in kilometers
     * @return a list of nearby venues that meet the requirements
     * @author James Chen
     */
    public List<VenueSearchResultResponse> filterNearbyVenues(
        final List<VenueSport> venueSports,
        final Collection<EarthCoordinate> memberCoordinates,
        final double maxDistance
    ) {
        final List<VenueSearchResultResponse> nearbyVenues = new ArrayList<>();
        for (final VenueSport venueSport : venueSports) {
            final Venue venue = venueSport.getVenue();
            final EarthCoordinate venueCoordinate = EarthCoordinates.createFrom(venue);

            // Calculate the distances to the venue for all members. Skip the venue if any member is
            // farther away than the maximum distance.
            boolean someoneIsTooFarAway = false;
            final List<Double> memberDistancesKm = new ArrayList<>();
            for (final EarthCoordinate memberCoordinate : memberCoordinates) {
                final double distance = EarthCoordinates.calculateDistanceKm(
                    memberCoordinate,
                    venueCoordinate
                );
                if (distance > maxDistance) {
                    someoneIsTooFarAway = true;
                    break;
                }

                memberDistancesKm.add(distance);
            }
            if (someoneIsTooFarAway) {
                continue;
            }

            nearbyVenues.add(Beans.create(VenueSearchResultResponse.class, (it) -> {
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
     * @param venueIds IDs of the venues whose opening intervals should be retrieved
     * @return a table mapping venue IDs to opening intervals ({@code OpeningInterval} instances)
     * @author James Chen
     */
    public Map<Long, List<OpeningInterval>> getIntervalListsByVenueId(final List<Long> venueIds) {
        return openingIntervalRepository.findByVenueIdIn(venueIds).stream().collect(
            Collectors.groupingBy(interval -> interval.getVenue().getId())
        );
    }

    /**
     * Checks whether the requested local date and time fall within the given opening interval.
     *
     * @param interval    an opening interval to check
     * @param requestedAt the requested local date and time
     * @return true if the requested time falls within the opening interval; false otherwise
     * @author Andrew Hsu, Codex, James Chen
     */
    public boolean isOpenAt(final OpeningInterval interval, final LocalDateTime requestedAt) {
        final int daysSinceOpening = Math.floorMod(
            requestedAt.getDayOfWeek().getValue() - interval.getDayOfWeek().getValue(),
            7
        );
        final LocalDateTime opensAt = requestedAt.toLocalDate().minusDays(daysSinceOpening).atTime(
            interval.getOpensAt()
        );
        LocalDateTime closesAt = opensAt.toLocalDate().atTime(interval.getClosesAt());
        if (!interval.getClosesAt().isAfter(interval.getOpensAt())) {
            closesAt = closesAt.plusDays(1);
        }
        return !requestedAt.isBefore(opensAt) && requestedAt.isBefore(closesAt);
    }

    /**
     * Checks whether the requested local date and time fall within any given opening interval.
     *
     * @param intervals   a list of opening intervals to check
     * @param requestedAt the requested local date and time
     * @return true if the requested time falls within any opening interval; false otherwise
     * @author James Chen
     */
    public boolean isOpenAt(
        final List<OpeningInterval> intervals,
        final LocalDateTime requestedAt
    ) {
        for (final OpeningInterval openingInterval : intervals) {
            if (isOpenAt(openingInterval, requestedAt)) {
                return true;
            }
        }

        return false;
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
        final ZoneId searchTimeZone = ZoneId.of(VenueConstant.SEARCH_TIMEZONE);
        final LocalDateTime now = LocalDateTime.now(searchTimeZone);
        if (requestedAt.isBefore(now)) {
            throw VenueException.REQUEST_IN_PAST.clone();
        }
    }

    /**
     * Creates a venue and records the authenticated client who submitted it.
     *
     * <p>
     * The request must already have passed field validation. The timezone is checked against the
     * application's recognized region identifiers before persistence. Coordinates are supplied by
     * the client; this method does not perform address geocoding.
     *
     * <p>
     * Each successful call creates a new venue. Matching names or addresses are not treated as
     * duplicates. Sports, prices, and opening intervals are populated separately.
     *
     * @param client  authenticated client submitting the venue
     * @param request validated venue information
     * @return the identifier assigned to the created venue
     * @throws VenueException if the timezone is not recognized
     * @author Vincent Lai, Codex, James Chen
     */
    @Transactional
    public Venue addVenue(final Client client, final CreateVenueRequest request) {
        final String timezone = request.getTimezone().strip();
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw VenueException.INVALID_TIMEZONE.clone();
        }

        return venueRepository.saveAndFlush(Beans.create(Venue.class, (it) -> {
            it.setName(request.getName().strip());
            it.setAddress(request.getAddress().strip());
            it.setLatitude(request.getLatitude());
            it.setLongitude(request.getLongitude());
            it.setTimezone(timezone);
            it.setCreatedByClient(client);
        }));
    }

    /**
     * Saves a user's rating and comment for a venue or one of its registered sports.
     *
     * <p>
     * A null sport identifier selects the entire venue. Otherwise, the venue must offer the
     * selected sport. If the same client and external user already have a review for the same venue
     * and sport, its rating and comment are replaced while its identifier and creation time remain.
     * Reviews for other sports or clients are independent.
     *
     * <p>
     * The venue row is locked before looking up an existing review. Concurrent submissions using
     * this method therefore serialize per venue, preventing duplicate first submissions. The lock
     * is held until the transaction completes. The database separately enforces review uniqueness.
     *
     * <p>
     * The request must already have passed field validation. External user identifiers are
     * preserved exactly; comments have surrounding whitespace removed.
     *
     * @param client  authenticated client submitting the review
     * @param venueId identifier of the venue being reviewed
     * @param request validated rating, comment, external user identifier, and optional sport
     * @return the identifier of the created or updated review
     * @throws VenueException if the venue does not exist or does not offer the requested sport
     * @author Vincent Lai, Codex, James Chen
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Review rateVenue(
        final Client client,
        final Long venueId,
        final RateVenueRequest request
    ) {
        final Venue venue = venueRepository.findByIdForUpdate(venueId).orElseThrow(
            VenueException.NOT_FOUND::clone
        );

        final Long sportId = request.getSportId();
        final Sport sport = sportId == null ? null : getVenueSport(venueId, sportId).getSport();

        // Update an existing review or create a new one.
        final Review review = reviewRepository.findExistingReview(
            client.getId(),
            request.getExternalUserId(),
            venueId,
            sportId
        ).orElseGet(() -> Beans.create(Review.class, (it) -> {
            it.setClient(client);
            it.setExternalUserId(request.getExternalUserId());
            it.setVenue(venue);
            it.setSport(sport);
        }));
        review.setRating(request.getRating());
        review.setCommentContent(request.getComment().strip());

        return reviewRepository.saveAndFlush(review);
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
    public VenueSport getVenueSport(final Long venueId, final Long sportId) {
        return venueSportRepository.findByVenueIdAndSportId(venueId, sportId).orElseThrow(
            VenueException.SPORT_NOT_OFFERED::clone
        );
    }
}
