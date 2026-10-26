package com.naisa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.FindVenuesRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.FindVenueResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Tests individual venue searches with mocked repositories and a fixed New York time.
 *
 * @author Andrew Hsu, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueSearchTest {
  private static final Long SPORT_ID = 3L;
  private static final ZoneId SEARCH_ZONE = ZoneId.of("America/New_York");
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 19, 12, 0);

  @Mock private VenueRepository venueRepository;
  @Mock private VenueSportRepository venueSportRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private SportRepository sportRepository;
  @Mock private OpeningIntervalRepository openingIntervalRepository;

  @InjectMocks private VenueService venueService;

  private MockedStatic<LocalDateTime> dateTimes;
  private FindVenuesRequest request;

  /**
   * Fixes the clock and creates a valid individual search request.
   *
   * @author Andrew Hsu, Codex
   */
  @BeforeEach
  void setUp() {
    dateTimes = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
    dateTimes.when(() -> LocalDateTime.now(SEARCH_ZONE)).thenReturn(NOW);
    request = new FindVenuesRequest();
    request.setLatitude(new BigDecimal("40.00"));
    request.setLongitude(new BigDecimal("-73.00"));
    request.setSportId(SPORT_ID);
    request.setDate(LocalDate.of(2026, 10, 27));
    request.setTime(LocalTime.NOON);
    request.setMaxDistance(new BigDecimal("2.00"));
    request.setBudget(new BigDecimal("20.00"));
  }

  /**
   * Restores real date-time behavior after each test.
   *
   * @author Andrew Hsu, Codex
   */
  @AfterEach
  void tearDown() {
    dateTimes.close();
  }

  /**
   * Returns venue fields and equal individual, center, and average distances.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void returnsMatchingVenue() {
    final VenueSport offering = offering(7L, 40.01);
    mockOfferings(List.of(offering));
    when(openingIntervalRepository.findByVenueIdIn(List.of(7L)))
        .thenReturn(List.of(interval(offering, DayOfWeek.TUESDAY, "09:00", "17:00")));

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).hasSize(1);
    final FindVenueResponse result = results.getFirst();
    assertThat(result.getVenueId()).isEqualTo(7L);
    assertThat(result.getName()).isEqualTo("Venue 7");
    assertThat(result.getHourlyPrice()).isEqualByComparingTo("20.00");
    assertThat(result.getIsIndoor()).isFalse();
    assertThat(result.isAvailable()).isTrue();
    // A latitude difference of 0.01 degrees is about 1.112 km at this longitude.
    assertThat(result.getDistanceKm()).isCloseTo(1.111949, within(0.000001));
    assertThat(result.getAverageDistanceKm()).isEqualTo(result.getDistanceKm());
    assertThat(result.getMemberDistancesKm()).containsExactly(result.getDistanceKm());
  }

  /**
   * Includes zero distance at a zero radius and excludes venues beyond the radius.
   *
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @CsvSource({"0.00, 1", "1.00, 1", "2.00, 2"})
  void appliesInclusiveRadius(final BigDecimal radius, final int expectedCount) {
    request.setMaxDistance(radius);
    final VenueSport atOrigin = offering(1L, 40.00);
    final VenueSport fartherAway = offering(2L, 40.01);
    mockOfferings(List.of(atOrigin, fartherAway));
    final List<Long> nearbyIds = expectedCount == 1 ? List.of(1L) : List.of(1L, 2L);
    when(openingIntervalRepository.findByVenueIdIn(nearbyIds))
        .thenReturn(
            expectedCount == 1
                ? List.of(interval(atOrigin, DayOfWeek.TUESDAY, "09:00", "17:00"))
                : List.of(
                    interval(atOrigin, DayOfWeek.TUESDAY, "09:00", "17:00"),
                    interval(fartherAway, DayOfWeek.TUESDAY, "09:00", "17:00")));

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results)
        .extracting(FindVenueResponse::getVenueId)
        .containsExactlyElementsOf(nearbyIds);
  }

  /**
   * Requests the selected sport and inclusive budget, including a zero budget.
   *
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @CsvSource({"0.00", "20.00"})
  void returnsEmptyWhenNoAffordableOfferingsExist(final BigDecimal budget) {
    request.setBudget(budget);
    mockOfferings(List.of());

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).isEmpty();
    verify(venueSportRepository).findBySportIdAndPriceLessThanEqual(SPORT_ID, budget);
    verifyNoInteractions(openingIntervalRepository);
  }

  /**
   * Stops before fetching hours when every offering is outside the search radius.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void returnsEmptyWhenAllVenuesAreTooFarAway() {
    mockOfferings(List.of(offering(1L, 41.00)));

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).isEmpty();
    verifyNoInteractions(openingIntervalRepository);
  }

  /**
   * Checks daytime, overnight, week boundary, and 24-hour intervals through the public method.
   *
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @CsvSource({
    "TUESDAY, 09:00, 17:00, 2026-10-27, 08:59:59, false",
    "TUESDAY, 09:00, 17:00, 2026-10-27, 09:00, true",
    "TUESDAY, 09:00, 17:00, 2026-10-27, 16:59:59.999999999, true",
    "TUESDAY, 09:00, 17:00, 2026-10-27, 17:00, false",
    "MONDAY, 09:00, 17:00, 2026-10-27, 12:00, false",
    "MONDAY, 22:00, 02:00, 2026-10-26, 21:59:59, false",
    "MONDAY, 22:00, 02:00, 2026-10-26, 22:00, true",
    "MONDAY, 22:00, 02:00, 2026-10-27, 01:59:59, true",
    "MONDAY, 22:00, 02:00, 2026-10-27, 02:00, false",
    "SUNDAY, 22:00, 02:00, 2026-10-26, 01:00, true",
    "SUNDAY, 22:00, 02:00, 2026-10-26, 02:00, false",
    "MONDAY, 09:00, 09:00, 2026-10-26, 09:00, true",
    "MONDAY, 09:00, 09:00, 2026-10-27, 08:59:59, true",
    "MONDAY, 09:00, 09:00, 2026-10-27, 09:00, false"
  })
  void appliesOpeningHours(
      final DayOfWeek day,
      final LocalTime opens,
      final LocalTime closes,
      final LocalDate date,
      final LocalTime time,
      final boolean expectedOpen) {
    request.setDate(date);
    request.setTime(time);
    final VenueSport offering = offering(1L, 40.00);
    mockOfferings(List.of(offering));
    when(openingIntervalRepository.findByVenueIdIn(List.of(1L)))
        .thenReturn(List.of(interval(offering, day, opens.toString(), closes.toString())));

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).hasSize(expectedOpen ? 1 : 0);
  }

  /**
   * Uses any matching interval and excludes venues with no stored hours.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void matchesAnyIntervalAndExcludesMissingHours() {
    final VenueSport withHours = offering(1L, 40.00);
    mockOfferings(List.of(withHours, offering(2L, 40.00)));
    when(openingIntervalRepository.findByVenueIdIn(List.of(1L, 2L)))
        .thenReturn(
            List.of(
                interval(withHours, DayOfWeek.TUESDAY, "06:00", "09:00"),
                interval(withHours, DayOfWeek.TUESDAY, "11:00", "17:00")));

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).extracting(FindVenueResponse::getVenueId).containsExactly(1L);
  }

  /**
   * Sorts by distance first and by venue ID when distances are equal.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void sortsByDistanceThenVenueId() {
    final List<VenueSport> offerings =
        List.of(offering(1L, 40.01), offering(9L, 40.00), offering(3L, 40.00));
    mockOfferings(offerings);
    when(openingIntervalRepository.findByVenueIdIn(List.of(1L, 9L, 3L)))
        .thenReturn(
            offerings.stream()
                .map(offering -> interval(offering, DayOfWeek.TUESDAY, "09:00", "17:00"))
                .toList());

    final List<FindVenueResponse> results = venueService.findVenues(request);

    assertThat(results).extracting(FindVenueResponse::getVenueId).containsExactly(3L, 9L, 1L);
  }

  /**
   * Rejects past dates and earlier times today before consulting repositories.
   *
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @CsvSource({"2026-10-18, 12:00", "2026-10-19, 11:59:59.999999999"})
  void rejectsPastRequest(final LocalDate date, final LocalTime time) {
    request.setDate(date);
    request.setTime(time);

    assertThatExceptionOfType(VenueException.class)
        .isThrownBy(() -> venueService.findVenues(request))
        .satisfies(
            exception -> {
              assertThat(exception.getCode()).isEqualTo("request.validation_failed");
              assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            });
    verifyNoInteractions(sportRepository, venueSportRepository, openingIntervalRepository);
  }

  /**
   * Accepts a request exactly at the fixed current time.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void acceptsCurrentTime() {
    request.setDate(NOW.toLocalDate());
    request.setTime(NOW.toLocalTime());
    mockOfferings(List.of());

    assertThat(venueService.findVenues(request)).isEmpty();
    verify(venueSportRepository).findBySportIdAndPriceLessThanEqual(SPORT_ID, request.getBudget());
  }

  /**
   * Rejects an unknown sport before fetching offerings or hours.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void rejectsUnknownSport() {
    when(sportRepository.existsById(SPORT_ID)).thenReturn(false);

    assertThatExceptionOfType(VenueException.class)
        .isThrownBy(() -> venueService.findVenues(request))
        .satisfies(
            exception -> {
              assertThat(exception.getCode()).isEqualTo("sport.not_found");
              assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
    verifyNoInteractions(venueSportRepository, openingIntervalRepository);
  }

  /**
   * Supplies offerings already filtered by sport and budget, as required by the repository
   * contract.
   *
   * @author Andrew Hsu, Codex
   */
  private void mockOfferings(final List<VenueSport> offerings) {
    when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    when(venueSportRepository.findBySportIdAndPriceLessThanEqual(SPORT_ID, request.getBudget()))
        .thenReturn(offerings);
  }

  /**
   * Creates an offering north of the fixed search origin.
   *
   * @author Andrew Hsu, Codex
   */
  private static VenueSport offering(final Long id, final double latitude) {
    final Venue venue = new Venue();
    venue.setId(id);
    venue.setName("Venue " + id);
    venue.setLatitude(latitude);
    venue.setLongitude(-73.00);
    final VenueSport offering = new VenueSport();
    offering.setVenue(venue);
    offering.setPrice(new BigDecimal("20.00"));
    offering.setIsIndoor(false);
    return offering;
  }

  /**
   * Creates a weekly opening interval for an offering's venue.
   *
   * @author Andrew Hsu, Codex
   */
  private static OpeningInterval interval(
      final VenueSport offering, final DayOfWeek day, final String opens, final String closes) {
    final OpeningInterval interval = new OpeningInterval();
    interval.setVenue(offering.getVenue());
    interval.setDayOfWeek(day);
    interval.setOpensAt(LocalTime.parse(opens));
    interval.setClosesAt(LocalTime.parse(closes));
    return interval;
  }
}
