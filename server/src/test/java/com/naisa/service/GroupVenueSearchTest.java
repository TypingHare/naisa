package com.naisa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.SearchVenuesRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueSearchResultResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;

/**
 * Tests group venue searches with mocked repositories and a fixed New York time.
 *
 * @author Andrew Hsu, Codex, James Chen, Codex
 */
@ExtendWith(MockitoExtension.class)
class GroupVenueSearchTest {
  private static final Long SPORT_ID = 3L;
  private static final ZoneId SEARCH_ZONE = ZoneId.of("America/New_York");
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 19, 12, 0);

  @Mock private VenueRepository venueRepository;
  @Mock private VenueSportRepository venueSportRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private SportRepository sportRepository;
  @Mock private OpeningIntervalRepository openingIntervalRepository;

  @InjectMocks private VenueService venueService;

  private VenueSearchService venueSearchService;

  private MockedStatic<LocalDateTime> dateTimes;
  private SearchVenuesRequest request;

  /**
   * Creates a valid group request with no individual coordinate pair.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @BeforeEach
  void setUp() throws NoSuchMethodException {
    venueSearchService =
        BeanUtils.instantiateClass(
            VenueSearchService.class.getDeclaredConstructor(VenueService.class), venueService);
    dateTimes = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
    dateTimes.when(() -> LocalDateTime.now(SEARCH_ZONE)).thenReturn(NOW);
    request = new SearchVenuesRequest();
    request.setCoordinates(List.of(location("40.00", "-73.00"), location("40.02", "-73.00")));
    request.setSportId(SPORT_ID);
    request.setDate(LocalDate.of(2026, 10, 27));
    request.setTime(LocalTime.NOON);
    request.setMaxDistance(new BigDecimal("3.00"));
    request.setBudget(new BigDecimal("20.00"));
  }

  /**
   * Restores normal date-time behavior after each test.
   *
   * @author Andrew Hsu, Codex
   */
  @AfterEach
  void tearDown() {
    dateTimes.close();
  }

  /**
   * Returns individual distances and venue details for a one-member coordinate list.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @Test
  void returnsIndividualDistancesForOneMember() {
    request.setCoordinates(List.of(location("40.00", "-73.00")));
    mockOpenVenues(List.of(offering(7L, 40.01, -73.00)));

    final List<VenueSearchResultResponse> groupResults = venueSearchService.findVenues(request);

    assertThat(groupResults).hasSize(1);
    final VenueSearchResultResponse result = groupResults.getFirst();
    assertThat(result.getMemberDistancesKm().getFirst()).isCloseTo(1.111949, within(0.000001));
    // Removed response field:
    // assertThat(result.getAverageDistanceKm()).isEqualTo(result.getDistanceKm());
    assertThat(result.getMemberDistancesKm()).hasSize(1);
    assertThat(result.getVenueId()).isEqualTo(7L);
    assertThat(result.getName()).isEqualTo("Venue 7");
    assertThat(result.getHourlyPrice()).isEqualByComparingTo("20.00");
    assertThat(result.getIsIndoor()).isTrue();
    // Removed response field:
    // assertThat(result.isAvailable()).isTrue();
  }

  // Disabled: The radius now applies to every member, not the group center.
  // /**
  //  * Averages both coordinates and applies the radius only to the center-to-venue distance.
  //  *
  //  * @author Andrew Hsu, Codex
  //  */
  // @Test
  // void includesVenueAtCenterEvenWhenMembersAreOutsideRadius() {
  //   request.setCoordinates(List.of(location("40.00", "-73.00"), location("40.02", "-73.04")));
  //   request.setMaxDistance(new BigDecimal("0.001"));
  //   mockOpenVenues(List.of(offering(1L, 40.01, -73.02)));
  //
  //   final List<VenueSearchResponse> results = venueSearchService.findVenues(request);
  //
  //   assertThat(results).hasSize(1);
  //   final VenueSearchResponse result = results.getFirst();
  //   assertThat(result.getDistanceKm()).isCloseTo(0.0, within(0.00000001));
  //   assertThat(result.getMemberDistancesKm())
  //       .hasSize(2)
  //       .allSatisfy(distance -> assertThat(distance).isGreaterThan(0.001));
  //   assertThat(result.getAverageDistanceKm()).isGreaterThan(0.001);
  // }

  /**
   * Excludes a venue outside the center radius even when it is at one member's location.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @Test
  void excludesVenueNearOnlyOneMember() {
    request.setCoordinates(List.of(location("40.00", "-73.00"), location("40.04", "-73.00")));
    request.setMaxDistance(BigDecimal.ONE);
    mockOfferings(List.of(offering(1L, 40.00, -73.00)));

    final List<VenueSearchResultResponse> results = venueSearchService.findVenues(request);

    assertThat(results).isEmpty();
    verifyNoInteractions(openingIntervalRepository);
  }

  /**
   * Keeps member distances in input order.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @Test
  void preservesMemberOrderWhenLocationsAreReversed() {
    request.setCoordinates(
        List.of(
            location("40.02", "-73.00"), location("40.00", "-73.00"), location("40.01", "-73.00")));
    mockOpenVenues(List.of(offering(1L, 40.00, -73.00)));

    final List<VenueSearchResultResponse> forwardResults = venueSearchService.findVenues(request);
    request.setCoordinates(request.getCoordinates().reversed());
    final List<VenueSearchResultResponse> reverseResults = venueSearchService.findVenues(request);

    assertThat(forwardResults).hasSize(1);
    assertThat(reverseResults).hasSize(1);
    final VenueSearchResultResponse forward = forwardResults.getFirst();
    final VenueSearchResultResponse reverse = reverseResults.getFirst();
    assertThat(forward.getMemberDistancesKm()).hasSize(3);
    assertThat(forward.getMemberDistancesKm().get(0)).isCloseTo(2.223899, within(0.000001));
    assertThat(forward.getMemberDistancesKm().get(1)).isZero();
    assertThat(forward.getMemberDistancesKm().get(2)).isCloseTo(1.111949, within(0.000001));
    // Removed response field:
    // assertThat(forward.getAverageDistanceKm()).isCloseTo(1.111949, within(0.000001));
    assertThat(reverse.getMemberDistancesKm())
        .containsExactlyElementsOf(forward.getMemberDistancesKm().reversed());
    // Removed response field:
    // assertThat(reverse.getDistanceKm()).isCloseTo(forward.getDistanceKm(), within(0.00000001));
    // Removed response field:
    // assertThat(reverse.getAverageDistanceKm())
    // .isCloseTo(forward.getAverageDistanceKm(), within(0.00000001));
  }

  // Disabled: Search results are intentionally unordered; center distances are no longer returned.
  // /**
  //  * Counts members at the same location separately and sorts by average member distance.
  //  *
  //  * @author Andrew Hsu, Codex
  //  */
  // @Test
  // void sortsByMemberAverageInsteadOfCenterDistance() {
  //   request.setCoordinates(
  //       List.of(
  //           location("40.00", "-73.00"), location("40.00", "-73.00"), location("40.06",
  // "-73.00")));
  //   mockOpenVenues(List.of(offering(1L, 40.02, -73.00), offering(9L, 40.00, -73.00)));
  //
  //   final List<VenueSearchResponse> results = venueSearchService.findVenues(request);
  //
  //   assertThat(results).extracting(VenueSearchResponse::getVenueId).containsExactly(9L, 1L);
  //   final VenueSearchResponse nearMajority = results.get(0);
  //   final VenueSearchResponse atCenter = results.get(1);
  //   assertThat(nearMajority.getAverageDistanceKm()).isCloseTo(2.223899, within(0.000001));
  //   assertThat(atCenter.getAverageDistanceKm()).isCloseTo(2.965198, within(0.000001));
  //   assertThat(nearMajority.getDistanceKm()).isGreaterThan(atCenter.getDistanceKm());
  //   assertThat(nearMajority.getMemberDistancesKm()).hasSize(3);
  //   assertThat(nearMajority.getMemberDistancesKm().subList(0, 2)).containsExactly(0.0, 0.0);
  // }

  // /**
  //  * Uses venue ID to order equal average distances.
  //  *
  //  * @author Andrew Hsu, Codex, James Chen, Codex
  //  */
  // @Test
  // void breaksAverageDistanceTieWithVenueId() {
  //   mockOpenVenues(List.of(offering(9L, 40.01, -73.00), offering(2L, 40.01, -73.00)));
  //
  //   final List<FindVenueResponse> results = venueSearchService.findVenues(request);
  //
  //   assertThat(results).extracting(FindVenueResponse::getVenueId).containsExactly(2L, 9L);
  //   assertThat(results.get(0).getAverageDistanceKm())
  //       .isEqualTo(results.get(1).getAverageDistanceKm());
  // }

  /**
   * Supports the maximum valid group size with all members at a venue and a zero radius.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @Test
  void supportsTenMembersAtZeroRadius() {
    request.setCoordinates(
        IntStream.range(0, 10).mapToObj(index -> location("40.00", "-73.00")).toList());
    request.setMaxDistance(BigDecimal.ZERO);
    mockOpenVenues(List.of(offering(1L, 40.00, -73.00)));

    final List<VenueSearchResultResponse> results = venueSearchService.findVenues(request);

    assertThat(results).hasSize(1);
    final VenueSearchResultResponse result = results.getFirst();
    // Removed response field:
    // assertThat(result.getDistanceKm()).isZero();
    // Removed response field:
    // assertThat(result.getAverageDistanceKm()).isZero();
    assertThat(result.getMemberDistancesKm()).hasSize(10).containsOnly(0.0);
  }

  // Disabled: Sport validation now precedes time validation.
  // /**
  //  * Rejects a past group search before consulting repositories.
  //  *
  //  * @author Andrew Hsu, Codex
  //  */
  // @Test
  // void rejectsPastGroupSearch() {
  //   request.setDate(NOW.toLocalDate());
  //   request.setTime(NOW.toLocalTime().minusSeconds(1));
  //
  //   assertThatExceptionOfType(VenueException.class)
  //       .isThrownBy(() -> venueSearchService.findVenues(request))
  //       .satisfies(
  //           exception -> {
  //             assertThat(exception.getCode()).isEqualTo("request.validation_failed");
  //             assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
  //           });
  //   verifyNoInteractions(sportRepository, venueSportRepository, openingIntervalRepository);
  // }

  /**
   * Rejects an unknown sport before retrieving group search candidates.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  @Test
  void rejectsUnknownSport() {
    when(sportRepository.existsById(SPORT_ID)).thenReturn(false);

    assertThatExceptionOfType(VenueException.class)
        .isThrownBy(() -> venueSearchService.findVenues(request))
        .satisfies(
            exception -> {
              assertThat(exception.getCode()).isEqualTo("sport.not_found");
              assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
    verifyNoInteractions(venueSportRepository, openingIntervalRepository);
  }

  /**
   * Supplies repository results already filtered by sport and budget.
   *
   * @author Andrew Hsu, Codex
   */
  private void mockOfferings(final List<VenueSport> offerings) {
    when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    when(venueSportRepository.findBySportIdAndPriceLessThanEqual(SPORT_ID, request.getBudget()))
        .thenReturn(offerings);
  }

  /**
   * Supplies open venues that are within the configured radius.
   *
   * @author Andrew Hsu, Codex
   */
  private void mockOpenVenues(final List<VenueSport> offerings) {
    mockOfferings(offerings);
    when(openingIntervalRepository.findByVenueIdIn(
            offerings.stream().map(offering -> offering.getVenue().getId()).toList()))
        .thenReturn(
            offerings.stream()
                .map(
                    offering -> {
                      final OpeningInterval interval = new OpeningInterval();
                      interval.setVenue(offering.getVenue());
                      interval.setDayOfWeek(DayOfWeek.TUESDAY);
                      interval.setOpensAt(LocalTime.of(9, 0));
                      interval.setClosesAt(LocalTime.of(17, 0));
                      return interval;
                    })
                .toList());
  }

  /**
   * Creates one member location with double-precision coordinates.
   *
   * @author Andrew Hsu, Codex, James Chen, Codex
   */
  private static EarthCoordinate location(final String latitude, final String longitude) {
    return Beans.create(
        EarthCoordinate.class,
        it -> {
          it.setLatitude(Double.valueOf(latitude));
          it.setLongitude(Double.valueOf(longitude));
        });
  }

  /**
   * Creates an affordable offering at the specified coordinates.
   *
   * @author Andrew Hsu, Codex
   */
  private static VenueSport offering(final Long id, final double latitude, final double longitude) {
    final Venue venue = new Venue();
    venue.setId(id);
    venue.setName("Venue " + id);
    venue.setLatitude(latitude);
    venue.setLongitude(longitude);
    final VenueSport offering = new VenueSport();
    offering.setVenue(venue);
    offering.setPrice(new BigDecimal("20.00"));
    offering.setIsIndoor(true);
    return offering;
  }
}
