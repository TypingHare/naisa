package com.naisa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.VenueComparisonRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.model.vo.VenueComparisonItem;
import com.naisa.model.vo.VenueComparisonResponse;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link VenueComparisonService}.
 *
 * <p>Repositories and {@link CrowdReportService} are replaced with Mockito mocks, so no database is
 * needed. The user is located at (40.0, -73.0); venues are placed north of the user so that a
 * larger latitude means a longer distance.
 *
 * @author Emily Wang, Claude Code, Andrew Hsu, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueComparisonServiceTest {
  private static final Long SPORT_ID = 1L;

  @Mock private VenueRepository venueRepository;
  @Mock private VenueSportRepository venueSportRepository;
  @Mock private SportRepository sportRepository;
  @Mock private OpeningIntervalRepository openingIntervalRepository;
  @Mock private CrowdReportService crowdReportService;

  @InjectMocks private VenueComparisonService venueComparisonService;

  /** Creates a request from the user's fixed location for the given venue IDs. */
  private static VenueComparisonRequest request(final Long... venueIds) {
    final VenueComparisonRequest request = new VenueComparisonRequest();
    request.setVenueIds(List.of(venueIds));
    request.setSportId(SPORT_ID);
    request.setLatitude(BigDecimal.valueOf(40.0));
    request.setLongitude(BigDecimal.valueOf(-73.0));
    return request;
  }

  /**
   * Registers a venue in the mocked repositories.
   *
   * @param id venue ID
   * @param latitude venue latitude; larger values are farther from the user
   * @param price hourly price of the sport
   * @param crowdLevel crowd level returned by the crowd report service, or null for UNKNOWN
   * @param intervals opening intervals of the venue
   */
  private void mockVenue(
      final Long id,
      final double latitude,
      final String price,
      final String crowdLevel,
      final List<OpeningInterval> intervals) {
    final Venue venue = new Venue();
    venue.setId(id);
    venue.setName("Venue " + id);
    venue.setLatitude(latitude);
    venue.setLongitude(-73.0);
    venue.setTimezone("UTC");
    when(venueRepository.findById(id)).thenReturn(Optional.of(venue));

    final VenueSport offering = new VenueSport();
    offering.setPrice(new BigDecimal(price));
    offering.setIsIndoor(true);
    when(venueSportRepository.findByVenueIdAndSportId(id, SPORT_ID))
        .thenReturn(Optional.of(offering));

    when(openingIntervalRepository.findByVenueId(id)).thenReturn(intervals);

    final CrowdStatusResponse status = new CrowdStatusResponse();
    status.setCrowdLevel(crowdLevel);
    when(crowdReportService.getCrowdStatus(id, SPORT_ID)).thenReturn(status);
  }

  /** Creates an opening interval on the given day. */
  private static OpeningInterval interval(
      final DayOfWeek day, final LocalTime opensAt, final LocalTime closesAt) {
    final OpeningInterval interval = new OpeningInterval();
    interval.setDayOfWeek(day);
    interval.setOpensAt(opensAt);
    interval.setClosesAt(closesAt);
    return interval;
  }

  /** Creates the same opening interval on every day of the week. */
  private static List<OpeningInterval> everyDay(final LocalTime opensAt, final LocalTime closesAt) {
    return Arrays.stream(DayOfWeek.values()).map(day -> interval(day, opensAt, closesAt)).toList();
  }

  /** Returns a fixed UTC time on Tuesday, 2026-10-27. */
  private static ZonedDateTime tuesdayAt(final int hour, final int minute) {
    return ZonedDateTime.of(2026, 10, 27, hour, minute, 0, 0, ZoneOffset.UTC);
  }

  /** Tests for sorting and the nearest and cheapest venues. */
  @Nested
  @DisplayName("distance and price")
  class DistanceAndPrice {
    @BeforeEach
    void setUp() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("sorts venues by distance and picks the nearest and cheapest")
    void sortsAndPicksNearestAndCheapest() {
      mockVenue(1L, 40.10, "30.00", null, List.of());
      mockVenue(2L, 40.01, "25.00", null, List.of());
      mockVenue(3L, 40.05, "10.00", null, List.of());

      final VenueComparisonResponse response = venueComparisonService.compare(request(1L, 2L, 3L));

      assertEquals(
          List.of(2L, 3L, 1L),
          response.getVenues().stream().map(VenueComparisonItem::getVenueId).toList());
      assertEquals(2L, response.getNearestVenueId());
      assertEquals(3L, response.getCheapestVenueId());
    }

    @Test
    @DisplayName("breaks a price tie with the lowest venue ID")
    void breaksPriceTieWithLowestId() {
      mockVenue(5L, 40.01, "20.00", null, List.of());
      mockVenue(2L, 40.05, "20.00", null, List.of());

      final VenueComparisonResponse response = venueComparisonService.compare(request(5L, 2L));

      assertEquals(2L, response.getCheapestVenueId());
    }
  }

  /** Tests for the least crowded venue. */
  @Nested
  @DisplayName("crowd level")
  class CrowdLevel {
    @BeforeEach
    void setUp() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("skips venues with UNKNOWN crowd level")
    void skipsUnknownVenues() {
      mockVenue(1L, 40.01, "10.00", null, List.of());
      mockVenue(2L, 40.02, "10.00", "BUSY", List.of());
      mockVenue(3L, 40.03, "10.00", "LIGHT", List.of());

      final VenueComparisonResponse response = venueComparisonService.compare(request(1L, 2L, 3L));

      assertEquals(3L, response.getLeastCrowdedVenueId());
      assertNull(response.getVenues().get(0).getCrowdLevel());
    }

    @Test
    @DisplayName("breaks a crowd level tie with the lowest venue ID")
    void breaksCrowdTieWithLowestId() {
      mockVenue(5L, 40.01, "10.00", "LIGHT", List.of());
      mockVenue(2L, 40.02, "10.00", "LIGHT", List.of());

      final VenueComparisonResponse response = venueComparisonService.compare(request(5L, 2L));

      assertEquals(2L, response.getLeastCrowdedVenueId());
    }

    @Test
    @DisplayName("returns null when every venue is UNKNOWN")
    void returnsNullWhenAllUnknown() {
      mockVenue(1L, 40.01, "10.00", null, List.of());
      mockVenue(2L, 40.02, "10.00", null, List.of());

      final VenueComparisonResponse response = venueComparisonService.compare(request(1L, 2L));

      assertNull(response.getLeastCrowdedVenueId());
    }
  }

  /** Tests for the opening status in the comparison result. */
  @Nested
  @DisplayName("opening status")
  class OpeningStatus {
    @BeforeEach
    void setUp() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    }

    /**
     * Checks open, closed, and unknown venues at a fixed time.
     *
     * @author Emily Wang, Claude Code, Andrew Hsu, Codex
     */
    @Test
    @DisplayName("reports open, closed, and unknown opening status")
    void reportsOpeningStatus() {
      mockVenue(1L, 40.01, "10.00", null, everyDay(LocalTime.NOON, LocalTime.NOON));
      mockVenue(
          2L,
          40.02,
          "10.00",
          null,
          List.of(interval(DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(15, 0))));
      mockVenue(3L, 40.03, "10.00", null, List.of());
      final ZonedDateTime fixedTime = tuesdayAt(13, 0);
      try (MockedStatic<ZonedDateTime> times =
          mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
        times.when(() -> ZonedDateTime.now(ZoneId.of("UTC"))).thenReturn(fixedTime);
        final List<VenueComparisonItem> venues =
            venueComparisonService.compare(request(1L, 2L, 3L)).getVenues();
        assertTrue(venues.get(0).getOpenNow());
        assertFalse(venues.get(1).getOpenNow());
        assertNull(venues.get(2).getOpenNow());
      }
    }
  }

  /**
   * Tests for {@link VenueComparisonService#isOpenAt} with fixed times, so that the results do not
   * depend on the real clock.
   */
  @Nested
  @DisplayName("isOpenAt")
  class IsOpenAt {
    private final List<OpeningInterval> daytime =
        List.of(interval(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
    private final List<OpeningInterval> mondayNight =
        List.of(interval(DayOfWeek.MONDAY, LocalTime.of(22, 0), LocalTime.of(2, 0)));
    private final List<OpeningInterval> tuesdayNight =
        List.of(interval(DayOfWeek.TUESDAY, LocalTime.of(22, 0), LocalTime.of(2, 0)));

    /**
     * Includes 24 hours from an equal opening and closing time, with an exclusive closing boundary.
     *
     * @author Andrew Hsu, Codex
     */
    @Test
    void checksTwentyFourHourInterval() {
      final List<OpeningInterval> fullDay =
          List.of(interval(DayOfWeek.TUESDAY, LocalTime.NOON, LocalTime.NOON));
      assertFalse(VenueComparisonService.isOpenAt(fullDay, tuesdayAt(11, 59)));
      assertTrue(VenueComparisonService.isOpenAt(fullDay, tuesdayAt(12, 0)));
      assertTrue(VenueComparisonService.isOpenAt(fullDay, tuesdayAt(11, 59).plusDays(1)));
      assertFalse(VenueComparisonService.isOpenAt(fullDay, tuesdayAt(12, 0).plusDays(1)));
    }

    @Test
    @DisplayName("includes the opening time and excludes the closing time")
    void checksDaytimeBoundaries() {
      assertFalse(VenueComparisonService.isOpenAt(daytime, tuesdayAt(8, 59)));
      assertTrue(VenueComparisonService.isOpenAt(daytime, tuesdayAt(9, 0)));
      assertTrue(VenueComparisonService.isOpenAt(daytime, tuesdayAt(16, 59)));
      assertFalse(VenueComparisonService.isOpenAt(daytime, tuesdayAt(17, 0)));
    }

    @Test
    @DisplayName("keeps an overnight interval open after midnight")
    void checksOvernightAfterMidnight() {
      assertTrue(VenueComparisonService.isOpenAt(mondayNight, tuesdayAt(1, 59)));
      assertFalse(VenueComparisonService.isOpenAt(mondayNight, tuesdayAt(2, 0)));
    }

    @Test
    @DisplayName("opens an overnight interval on its own day")
    void checksOvernightBeforeMidnight() {
      assertFalse(VenueComparisonService.isOpenAt(tuesdayNight, tuesdayAt(21, 59)));
      assertTrue(VenueComparisonService.isOpenAt(tuesdayNight, tuesdayAt(22, 0)));
    }

    @Test
    @DisplayName("ignores intervals on other days")
    void ignoresOtherDays() {
      assertFalse(VenueComparisonService.isOpenAt(daytime, tuesdayAt(12, 0).plusDays(1)));
      assertFalse(VenueComparisonService.isOpenAt(mondayNight, tuesdayAt(1, 0).plusDays(1)));
    }
  }

  /** Tests for rejected requests. */
  @Nested
  @DisplayName("errors")
  class Errors {
    @Test
    @DisplayName("throws request.validation_failed for duplicate venue IDs")
    void rejectsDuplicateIds() {
      final VenueException exception =
          assertThrows(VenueException.class, () -> venueComparisonService.compare(request(1L, 1L)));

      assertEquals("request.validation_failed", exception.getCode());
    }

    @Test
    @DisplayName("throws sport.not_found when the sport does not exist")
    void rejectsUnknownSport() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(false);

      final VenueException exception =
          assertThrows(VenueException.class, () -> venueComparisonService.compare(request(1L, 2L)));

      assertEquals("sport.not_found", exception.getCode());
    }

    @Test
    @DisplayName("throws venue.not_found when a venue does not exist")
    void rejectsUnknownVenue() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
      when(venueRepository.findById(1L)).thenReturn(Optional.empty());

      final VenueException exception =
          assertThrows(VenueException.class, () -> venueComparisonService.compare(request(1L, 2L)));

      assertEquals("venue.not_found", exception.getCode());
    }

    @Test
    @DisplayName("throws venue.sport_not_offered when a venue does not offer the sport")
    void rejectsSportNotOffered() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
      when(venueRepository.findById(1L)).thenReturn(Optional.of(new Venue()));
      when(venueSportRepository.findByVenueIdAndSportId(1L, SPORT_ID)).thenReturn(Optional.empty());

      final VenueException exception =
          assertThrows(VenueException.class, () -> venueComparisonService.compare(request(1L, 2L)));

      assertEquals("venue.sport_not_offered", exception.getCode());
    }
  }
}
