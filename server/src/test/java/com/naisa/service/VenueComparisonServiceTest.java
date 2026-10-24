package com.naisa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link VenueComparisonService}.
 *
 * <p>Repositories and {@link CrowdReportService} are replaced with Mockito mocks, so no database is
 * needed. The user is located at (40.0, -73.0); venues are placed north of the user so that a
 * larger latitude means a longer distance.
 *
 * @author Emily Wang, Claude Code
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

  /** Creates an opening interval that lasts the whole given day. */
  private static OpeningInterval allDay(final DayOfWeek day) {
    final OpeningInterval interval = new OpeningInterval();
    interval.setDayOfWeek(day);
    interval.setOpensAt(LocalTime.MIN);
    interval.setClosesAt(LocalTime.MAX);
    return interval;
  }

  /**
   * Creates an overnight interval, which closes on the next day because the closing time is earlier
   * than the opening time.
   */
  private static OpeningInterval overnight(
      final DayOfWeek day, final LocalTime opensAt, final LocalTime closesAt) {
    final OpeningInterval interval = new OpeningInterval();
    interval.setDayOfWeek(day);
    interval.setOpensAt(opensAt);
    interval.setClosesAt(closesAt);
    return interval;
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

  /** Tests for the opening status. */
  @Nested
  @DisplayName("opening status")
  class OpeningStatus {
    @BeforeEach
    void setUp() {
      when(sportRepository.existsById(SPORT_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("reports open, closed, and unknown opening status")
    void reportsOpeningStatus() {
      final DayOfWeek today = ZonedDateTime.now(ZoneOffset.UTC).getDayOfWeek();
      mockVenue(1L, 40.01, "10.00", null, List.of(allDay(today)));
      mockVenue(2L, 40.02, "10.00", null, List.of(allDay(today.plus(2))));
      mockVenue(3L, 40.03, "10.00", null, List.of());

      final List<VenueComparisonItem> venues =
          venueComparisonService.compare(request(1L, 2L, 3L)).getVenues();

      assertTrue(venues.get(0).getOpenNow());
      assertFalse(venues.get(1).getOpenNow());
      assertNull(venues.get(2).getOpenNow());
    }

    @Test
    @DisplayName("handles intervals that close after midnight")
    void handlesOvernightIntervals() {
      final DayOfWeek today = ZonedDateTime.now(ZoneOffset.UTC).getDayOfWeek();
      // Opened yesterday just before midnight and is still open today.
      final OpeningInterval fromYesterday =
          overnight(today.minus(1), LocalTime.of(23, 59, 59), LocalTime.of(23, 59, 58));
      // Opened today just after midnight and closes tomorrow.
      final OpeningInterval fromToday =
          overnight(today, LocalTime.of(0, 0, 2), LocalTime.of(0, 0, 1));
      // Same hours, but on a day that is neither today nor yesterday.
      final OpeningInterval otherDay =
          overnight(today.plus(2), LocalTime.of(0, 0, 2), LocalTime.of(0, 0, 1));
      mockVenue(1L, 40.01, "10.00", null, List.of(fromYesterday));
      mockVenue(2L, 40.02, "10.00", null, List.of(fromToday));
      mockVenue(3L, 40.03, "10.00", null, List.of(otherDay));

      final List<VenueComparisonItem> venues =
          venueComparisonService.compare(request(1L, 2L, 3L)).getVenues();

      assertTrue(venues.get(0).getOpenNow());
      assertTrue(venues.get(1).getOpenNow());
      assertFalse(venues.get(2).getOpenNow());
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
