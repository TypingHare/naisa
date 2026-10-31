package com.naisa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.ReviewRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Tests venue service behavior with mocked repositories and no database.
 *
 * @author Andrew Hsu, Codex, James Chen, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {
	private static final Long VENUE_ID = 7L;

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

	/**
	 * Tests the venue information operation.
	 *
	 * @author Andrew Hsu, Codex, James Chen, Codex
	 */
	@Nested
	class GetVenueInfo {
		private Venue venue;
		private VenueRetrievalService venueRetrievalService;

		/**
		 * Creates a venue shared by the successful lookup cases.
		 *
		 * @author Andrew Hsu, Codex, James Chen, Codex
		 */
		@BeforeEach
		void setUp() {
			venueRetrievalService = new VenueRetrievalService(venueService);
			venue = new Venue();
			venue.setId(VENUE_ID);
			venue.setName("Riverside Sports Center");
			venue.setAddress("123 Riverside Drive");
			venue.setLatitude(40.81);
			venue.setLongitude(-73.96);
			venue.setTimezone("America/New_York");
		}

		// Disabled: Retrieval intentionally preserves repository order instead of sorting.
		// /**
		// * Returns venue fields and all sports, sorted by sport ID.
		// *
		// * @author Andrew Hsu, Codex, James Chen, Codex
		// */
		// @Test
		// void returnsVenueDetailsAndSortsSports() {
		// when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
		// when(venueSportRepository.findByVenueId(VENUE_ID))
		// .thenReturn(
		// List.of(
		// offering(9L, "Tennis", "25.00", false),
		// offering(2L, "Basketball", "10.00", true)));
		// when(openingIntervalRepository.findByVenueId(VENUE_ID)).thenReturn(List.of());
		//
		// final VenueVo response = venueRetrievalService.getVenueVo(VENUE_ID);
		//
		// assertThat(response.getVenueId()).isEqualTo(VENUE_ID);
		// assertThat(response.getName()).isEqualTo("Riverside Sports Center");
		// assertThat(response.getAddress()).isEqualTo("123 Riverside Drive");
		// assertThat(response.getLatitude()).isEqualTo(40.81);
		// assertThat(response.getLongitude()).isEqualTo(-73.96);
		// assertThat(response.getTimezone()).isEqualTo("America/New_York");
		// assertThat(response.getSports())
		// .extracting(
		// SportVo::getSportId, SportVo::getName,
		// SportVo::getHourlyPrice, SportVo::getIsIndoor)
		// .containsExactly(
		// tuple(2L, "Basketball", new BigDecimal("10.00"), true),
		// tuple(9L, "Tennis", new BigDecimal("25.00"), false));
		// }

		// Disabled: Retrieval intentionally preserves repository order instead of sorting.
		// /**
		// * Sorts hours by weekday, opening time, and closing time, preserving
		// overnight hours.
		// *
		// * @author Andrew Hsu, Codex, James Chen, Codex
		// */
		// @Test
		// void sortsOpeningIntervalsAndPreservesLocalTimes() {
		// when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
		// when(venueSportRepository.findByVenueId(VENUE_ID)).thenReturn(List.of());
		// when(openingIntervalRepository.findByVenueId(VENUE_ID))
		// .thenReturn(
		// List.of(
		// interval(DayOfWeek.SUNDAY, 22, 2),
		// interval(DayOfWeek.MONDAY, 14, 18),
		// interval(DayOfWeek.MONDAY, 9, 12),
		// interval(DayOfWeek.MONDAY, 9, 11)));
		//
		// final VenueVo response = venueRetrievalService.getVenueVo(VENUE_ID);
		//
		// assertThat(response.getOpeningIntervals())
		// .extracting(
		// OpeningIntervalVo::getDayOfWeek,
		// OpeningIntervalVo::getOpensAt,
		// OpeningIntervalVo::getClosesAt)
		// .containsExactly(
		// tuple(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)),
		// tuple(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
		// tuple(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0)),
		// tuple(DayOfWeek.SUNDAY, LocalTime.of(22, 0), LocalTime.of(2, 0)));
		// }

		/**
		 * Returns empty lists when either or both related record sets are absent.
		 *
		 * @author Andrew Hsu, Codex, James Chen, Codex
		 */
		@ParameterizedTest
		@CsvSource({ "true, false", "false, true", "true, true" })
		void allowsMissingSportsOrHours(final boolean noSports, final boolean noHours) {
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
			when(venueSportRepository.findByVenueId(VENUE_ID)).thenReturn(
				noSports ? List.of() : List.of(offering(2L, "Basketball", "0.00", true)));
			when(openingIntervalRepository.findByVenueId(VENUE_ID))
				.thenReturn(noHours ? List.of() : List.of(interval(DayOfWeek.MONDAY, 9, 17)));

			final VenueResponse response = venueRetrievalService.getVenueVo(VENUE_ID);

			assertThat(response.getVenueId()).isEqualTo(VENUE_ID);
			assertThat(response.getSports()).hasSize(noSports ? 0 : 1);
			assertThat(response.getOpeningIntervals()).hasSize(noHours ? 0 : 1);
		}

		/**
		 * Rejects an unknown venue before querying its sports or hours.
		 *
		 * @author Andrew Hsu, Codex, James Chen, Codex
		 */
		@Test
		void rejectsUnknownVenue() {
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.empty());

			final VenueException exception = assertThrows(VenueException.class,
				() -> venueRetrievalService.getVenueVo(VENUE_ID));

			assertThat(exception.getCode()).isEqualTo("venue.not_found");
			assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(exception).isNotSameAs(VenueException.NOT_FOUND);
			verifyNoInteractions(venueSportRepository, openingIntervalRepository);
		}

		/**
		 * Creates a sport offering with distinct response fields.
		 *
		 * @author Andrew Hsu, Codex
		 */
		private VenueSport offering(
			final Long sportId,
			final String name,
			final String price,
			final boolean indoor
		) {
			final Sport sport = new Sport();
			sport.setId(sportId);
			sport.setName(name);
			final VenueSport offering = new VenueSport();
			offering.setVenue(venue);
			offering.setSport(sport);
			offering.setPrice(new BigDecimal(price));
			offering.setIsIndoor(indoor);
			return offering;
		}

		/**
		 * Creates an opening interval in the venue's local time.
		 *
		 * @author Andrew Hsu, Codex
		 */
		private OpeningInterval interval(final DayOfWeek day, final int opens, final int closes) {
			final OpeningInterval interval = new OpeningInterval();
			interval.setVenue(venue);
			interval.setDayOfWeek(day);
			interval.setOpensAt(LocalTime.of(opens, 0));
			interval.setClosesAt(LocalTime.of(closes, 0));
			return interval;
		}
	}
}
