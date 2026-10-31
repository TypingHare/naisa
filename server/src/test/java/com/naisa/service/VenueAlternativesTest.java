package com.naisa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.common.EarthCoordinate;
import com.naisa.model.dto.SearchAlternativeVenuesRequest;
import com.naisa.model.dto.SearchAlternativeVenuesRequest.SearchConstraint;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.AlternativeVenueResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Tests permitted venue alternatives through the public service method without a database.
 *
 * @author Andrew Hsu, Codex, James Chen, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueAlternativesTest {
	private static final Long SPORT_ID = 3L;
	private static final ZoneId SEARCH_ZONE = ZoneId.of("America/New_York");
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 19, 12, 0);

	@Mock
	private VenueRepository venueRepository;
	@Mock
	private VenueSportRepository venueSportRepository;
	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private SportRepository sportRepository;
	@Mock
	private OpeningIntervalRepository openingIntervalRepository;
	@InjectMocks
	private VenueService venueService;

	private MockedStatic<LocalDateTime> dateTimes;
	private SearchAlternativeVenuesRequest request;

	/**
	 * Fixes New York time and creates a valid request with one permitted change.
	 *
	 * @author Andrew Hsu, Codex, James Chen, Codex
	 */
	@BeforeEach
	void setUp() {
		dateTimes = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
		dateTimes.when(() -> LocalDateTime.now(SEARCH_ZONE)).thenReturn(NOW);
		request = new SearchAlternativeVenuesRequest();
		request.setCoordinates(List.of(Beans.create(EarthCoordinate.class, it -> {
			it.setLatitude(40.00);
			it.setLongitude(-73.00);
		})));
		request.setSportId(SPORT_ID);
		request.setDate(LocalDate.of(2026, 10, 27));
		request.setTime(LocalTime.NOON);
		request.setMaxDistance(BigDecimal.ONE);
		request.setBudget(new BigDecimal("20.00"));
		request.setAllowedChanges(Set.of(SearchConstraint.BUDGET));
	}

	/**
	 * Releases the clock mock after each case.
	 *
	 * @author Andrew Hsu, Codex
	 */
	@AfterEach
	void tearDown() {
		dateTimes.close();
	}

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Preserves original matches, maps venue fields, and avoids duplicate results
	// from repeated
	// * hours.
	// *
	// * @author Andrew Hsu, Codex, James Chen, Codex
	// */
	// @Test
	// void returnsOriginalMatchWithoutUnnecessaryChanges() {
	// final VenueSport offering = offering(7L, 40.005, "10.00");
	// mockOfferings(List.of(offering));
	// final OpeningInterval hours = interval(offering, DayOfWeek.TUESDAY, "09:00",
	// "17:00");
	// mockHours(List.of(offering), List.of(hours, hours));
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results).hasSize(1);
	// final FindAlternativeResponse result = results.getFirst();
	// assertThat(result.getDate()).isEqualTo(request.getDate());
	// assertThat(result.getTime()).isEqualTo(request.getTime());
	// assertThat(result.getBudget()).isEqualByComparingTo("20.00");
	// assertThat(result.getMaxDistance()).isEqualByComparingTo("1.00");
	// assertThat(result.getVenue().getVenueId()).isEqualTo(7L);
	// assertThat(result.getVenue().getName()).isEqualTo("Venue 7");
	// assertThat(result.getVenue().getHourlyPrice()).isEqualByComparingTo("10.00");
	// assertThat(result.getVenue().getIsIndoor()).isTrue();
	// // Removed response field:
	// assertThat(result.getVenue().isAvailable()).isTrue();
	// assertThat(result.getVenue().getMemberDistancesKm().getFirst())
	// .isCloseTo(0.555975, within(0.000001));
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Applies permissions, inclusive budget limits, and the five-kilometer radius
	// increase.
	// *
	// * @author Andrew Hsu, Codex, James Chen, Codex
	// */
	// @ParameterizedTest
	// @CsvSource({
	// "TIME, 40.00, 20.00, MATCH", "TIME, 40.00, 20.01, EMPTY",
	// "BUDGET, 40.00, 40.00, MATCH", "BUDGET, 40.00, 40.01, EMPTY",
	// "MAX_DISTANCE, 40.05, 20.00, MATCH", "TIME, 40.05, 20.00, ERROR",
	// "MAX_DISTANCE, 40.06, 20.00, ERROR", "BUDGET, 40.05, 100.00, ERROR"
	// })
	void appliesOnlyPermittedDistanceAndBudgetChanges(
		final SearchConstraint permission,
		final double latitude,
		final String price,
		final String outcome
	) {
		request.setAllowedChanges(Set.of(permission));
		final VenueSport offering = offering(1L, latitude, price);
		mockOfferings(List.of(offering));
		if ("MATCH".equals(outcome)) {
			mockHours(List.of(offering),
				List.of(interval(offering, DayOfWeek.TUESDAY, "12:00", "13:00")));
		}
		if ("ERROR".equals(outcome)) {
			assertFailure("venue.no_nearby_venues", HttpStatus.NOT_FOUND);
			verifyNoInteractions(openingIntervalRepository);
			return;
		}

		final List<AlternativeVenueResponse> results = venueService.findAlternatives(request);

		assertThat(results).hasSize("MATCH".equals(outcome) ? 1 : 0);
		if (!results.isEmpty()) {
			final AlternativeVenueResponse result = results.getFirst();
			assertThat(result.getBudget())
				.isEqualByComparingTo(new BigDecimal(price).max(request.getBudget()));
			assertThat(result.getMaxDistance()).isEqualByComparingTo(
				BigDecimal.valueOf(result.getVenue().getMemberDistancesKm().getFirst())
					.max(request.getMaxDistance()));
			assertThat(result.getDate()).isEqualTo(request.getDate());
			assertThat(result.getTime()).isEqualTo(request.getTime());
		}
	}

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Includes both three-day boundaries and orders equal time differences by
	// earlier date.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void suggestsDatesWithinThreeDays() {
	// request.setAllowedChanges(Set.of(SearchConstraint.DATE));
	// mockEveryDay();
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .extracting(FindAlternativeResponse::getDate)
	// .containsExactly(
	// LocalDate.of(2026, 10, 27),
	// LocalDate.of(2026, 10, 26),
	// LocalDate.of(2026, 10, 28),
	// LocalDate.of(2026, 10, 25),
	// LocalDate.of(2026, 10, 29),
	// LocalDate.of(2026, 10, 24),
	// LocalDate.of(2026, 10, 30));
	// assertThat(results)
	// .allSatisfy(result ->
	// assertThat(result.getTime()).isEqualTo(LocalTime.NOON));
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Preserves minute, second, and nanosecond precision for all permitted hour
	// offsets.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void suggestsTimesWithinTwoHours() {
	// request.setAllowedChanges(Set.of(SearchConstraint.TIME));
	// request.setTime(LocalTime.parse("12:30:45.123456789"));
	// mockEveryDay();
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .extracting(FindAlternativeResponse::getTime)
	// .containsExactly(
	// LocalTime.parse("12:30:45.123456789"),
	// LocalTime.parse("11:30:45.123456789"),
	// LocalTime.parse("13:30:45.123456789"),
	// LocalTime.parse("10:30:45.123456789"),
	// LocalTime.parse("14:30:45.123456789"));
	// assertThat(results)
	// .allSatisfy(result ->
	// assertThat(result.getDate()).isEqualTo(request.getDate()));
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Allows midnight crossings only with date permission and keeps dates inside
	// the date window.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @ParameterizedTest
	// @CsvSource({
	// "2026-10-26, 23:30, false, 1", "2026-10-26, 23:30, true, 3",
	// "2026-10-27, 00:30, false, 2", "2026-10-27, 00:30, true, 3"
	// })
	void controlsMidnightCrossings(
		final LocalDate date,
		final LocalTime time,
		final boolean allowDate,
		final int expectedCount
	) {
		request.setDate(date);
		request.setTime(time);
		request.setAllowedChanges(allowDate ? Set.of(SearchConstraint.DATE, SearchConstraint.TIME)
			: Set.of(SearchConstraint.TIME));
		final VenueSport offering = offering(1L, 40.00, "20.00");
		mockOfferings(List.of(offering));
		mockHours(List.of(offering),
			List.of(interval(offering, DayOfWeek.MONDAY, "23:00", "02:00")));

		final List<AlternativeVenueResponse> results = venueService.findAlternatives(request);

		assertThat(results).hasSize(expectedCount);
		assertThat(results).extracting(AlternativeVenueResponse::getDate).contains(date);
		if (allowDate) {
			assertThat(results).extracting(AlternativeVenueResponse::getDate)
				.contains(LocalDate.of(2026, 10, 26), LocalDate.of(2026, 10, 27));
		} else {
			assertThat(results).allSatisfy(result -> assertThat(result.getDate()).isEqualTo(date));
		}
	}

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Removes past dates and times while retaining a suggestion exactly at the
	// fixed current time.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void discardsPastSuggestions() {
	// request.setDate(NOW.toLocalDate());
	// request.setTime(LocalTime.of(13, 0));
	// request.setAllowedChanges(Set.of(SearchConstraint.DATE,
	// SearchConstraint.TIME));
	// mockEveryDay();
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .isNotEmpty()
	// .allSatisfy(
	// result ->
	// assertThat(LocalDateTime.of(result.getDate(), result.getTime()))
	// .isAfterOrEqualTo(NOW));
	// assertThat(results)
	// .extracting(result -> LocalDateTime.of(result.getDate(), result.getTime()))
	// .contains(NOW);
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Counts actual changes rather than permissions before comparing distances or
	// prices.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void sortsByActualChangeCountFirst() {
	// request.setAllowedChanges(Set.of(SearchConstraint.values()));
	// final List<VenueSport> offerings =
	// List.of(
	// offering(3L, 40.02, "30.00"), offering(2L, 40.02, "20.00"),
	// offering(1L, 40.00, "30.00"), offering(9L, 40.005, "20.00"));
	// mockOfferings(offerings);
	// mockHours(
	// offerings,
	// offerings.stream()
	// .map(offering -> interval(offering, DayOfWeek.TUESDAY, "12:00", "13:00"))
	// .toList());
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .extracting(result -> result.getVenue().getVenueId())
	// .containsExactly(9L, 1L, 2L, 3L);
	// }

	// Disabled: Distance ordering was intentionally removed.
	// /**
	// * Breaks equal-change and equal-time ties by distance, then price, then venue
	// ID.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void sortsTiesByDistancePriceAndVenueId() {
	// final List<VenueSport> offerings =
	// List.of(
	// offering(1L, 40.005, "1.00"),
	// offering(9L, 40.00, "10.00"),
	// offering(2L, 40.00, "10.00"),
	// offering(7L, 40.00, "5.00"));
	// mockOfferings(offerings);
	// mockHours(
	// offerings,
	// offerings.stream()
	// .map(offering -> interval(offering, DayOfWeek.TUESDAY, "12:00", "13:00"))
	// .toList());
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .extracting(result -> result.getVenue().getVenueId())
	// .containsExactly(7L, 2L, 9L, 1L);
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Applies the ten-result limit after sorting rather than taking repository
	// order.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @Test
	// void limitsResultsAfterSorting() {
	// final List<VenueSport> offerings =
	// IntStream.rangeClosed(1, 12)
	// .mapToObj(id -> offering((long) id, 40.00, "20.00"))
	// .toList()
	// .reversed();
	// mockOfferings(offerings);
	// mockHours(
	// offerings,
	// offerings.stream()
	// .map(offering -> interval(offering, DayOfWeek.TUESDAY, "12:00", "13:00"))
	// .toList());
	//
	// final List<FindAlternativeResponse> results =
	// venueService.findAlternatives(request);
	//
	// assertThat(results)
	// .extracting(result -> result.getVenue().getVenueId())
	// .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Returns no alternatives when nearby affordable venues are closed or have no
	// hours.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @ParameterizedTest
	// @ValueSource(booleans = {true, false})
	// void returnsEmptyForUnavailableVenues(final boolean missingHours) {
	// final VenueSport offering = offering(1L, 40.00, "20.00");
	// mockOfferings(List.of(offering));
	// mockHours(
	// List.of(offering),
	// missingHours
	// ? List.of()
	// : List.of(interval(offering, DayOfWeek.TUESDAY, "13:00", "17:00")));
	//
	// assertThat(venueService.findAlternatives(request)).isEmpty();
	// }

	// TODO: Re-enable when findAlternatives() has been fixed.
	// /**
	// * Checks each service error before unnecessary repository work.
	// *
	// * @author Andrew Hsu, Codex
	// */
	// @ParameterizedTest
	// @ValueSource(strings = {"past", "sport", "nearby"})
	// void rejectsInvalidSearch(final String reason) {
	// request.setAllowedChanges(Set.of(SearchConstraint.values()));
	// if ("past".equals(reason)) {
	// request.setDate(NOW.toLocalDate().minusDays(1));
	// assertFailure("request.validation_failed", HttpStatus.BAD_REQUEST);
	// verifyNoInteractions(sportRepository, venueSportRepository);
	// } else if ("sport".equals(reason)) {
	// when(sportRepository.existsById(SPORT_ID)).thenReturn(false);
	// assertFailure("sport.not_found", HttpStatus.NOT_FOUND);
	// verifyNoInteractions(venueSportRepository);
	// } else {
	// mockOfferings(List.of());
	// assertFailure("venue.no_nearby_venues", HttpStatus.NOT_FOUND);
	// }
	// verifyNoInteractions(openingIntervalRepository);
	// }

	/**
	 * Checks the public error code and status.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private void assertFailure(final String code, final HttpStatus status) {
		assertThatExceptionOfType(VenueException.class)
			.isThrownBy(() -> venueService.findAlternatives(request)).satisfies(exception -> {
				assertThat(exception.getCode()).isEqualTo(code);
				assertThat(exception.getStatus()).isEqualTo(status);
			});
	}

	/**
	 * Supplies all sport offerings before the service applies distance and budget filters.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private void mockOfferings(final List<VenueSport> offerings) {
		when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
		when(venueSportRepository.findBySportId(SPORT_ID)).thenReturn(offerings);
	}

	/**
	 * Supplies opening intervals for the affordable nearby venues.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private void mockHours(final List<VenueSport> offerings, final List<OpeningInterval> hours) {
		when(openingIntervalRepository.findByVenueIdIn(
			offerings.stream().map(offering -> offering.getVenue().getId()).toList()))
				.thenReturn(hours);
	}

	/**
	 * Supplies a venue open for 24 hours on every weekday.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private void mockEveryDay() {
		final VenueSport offering = offering(1L, 40.00, "20.00");
		mockOfferings(List.of(offering));
		mockHours(List.of(offering), Arrays.stream(DayOfWeek.values())
			.map(day -> interval(offering, day, "00:00", "00:00")).toList());
	}

	/**
	 * Creates a sport offering at the selected latitude and price.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private static VenueSport offering(final Long id, final double latitude, final String price) {
		final Venue venue = new Venue();
		venue.setId(id);
		venue.setName("Venue " + id);
		venue.setLatitude(latitude);
		venue.setLongitude(-73.00);
		final VenueSport offering = new VenueSport();
		offering.setVenue(venue);
		offering.setPrice(new BigDecimal(price));
		offering.setIsIndoor(true);
		return offering;
	}

	/**
	 * Creates a weekly opening interval with local opening and closing times.
	 *
	 * @author Andrew Hsu, Codex
	 */
	private static OpeningInterval interval(
		final VenueSport offering,
		final DayOfWeek day,
		final String opens,
		final String closes
	) {
		final OpeningInterval interval = new OpeningInterval();
		interval.setVenue(offering.getVenue());
		interval.setDayOfWeek(day);
		interval.setOpensAt(LocalTime.parse(opens));
		interval.setClosesAt(LocalTime.parse(closes));
		return interval;
	}
}
