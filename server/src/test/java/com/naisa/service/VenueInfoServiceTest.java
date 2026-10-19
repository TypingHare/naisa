package com.naisa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueInfoException;
import com.naisa.model.dto.VenueInfoRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.VenueInfo;
import com.naisa.repository.VenueInfoHoursRepository;
import com.naisa.repository.VenueInfoSportRepository;
import com.naisa.repository.VenueInfoVenueRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Checks venue info lookup and mapping rules.
 *
 * @author Andrew Hsu, Codex
 */
@ExtendWith(MockitoExtension.class)
class VenueInfoServiceTest {
  @Mock private VenueInfoVenueRepository venueRepository;
  @Mock private VenueInfoSportRepository sportRepository;
  @Mock private VenueInfoHoursRepository hoursRepository;
  private VenueInfoService service;

  /**
   * Sets up the service.
   *
   * @author Andrew Hsu, Codex
   */
  @BeforeEach
  void setUp() {
    service = new VenueInfoService(venueRepository, sportRepository, hoursRepository);
  }

  /**
   * Returns stored values in a stable order for an ID lookup.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void idLookupReturnsStoredDetailsInOrder() {
    final Venue venue = venue(42, "Riverside Park");
    when(venueRepository.findById(42L)).thenReturn(Optional.of(venue));
    when(sportRepository.findByVenueId(42L))
        .thenReturn(
            List.of(
                sport(venue, "Tennis", "20.00", false, "Hard court"),
                sport(venue, "Basketball", "0.00", true, null)));
    when(hoursRepository.findByVenueId(42L))
        .thenReturn(
            List.of(
                hours(venue, "SUNDAY", "10:00", "20:00"),
                hours(venue, "MONDAY", "09:00", "12:00"),
                hours(venue, "MONDAY", "14:00", "22:00")));

    final VenueInfo result = service.getVenueInfo(request("42", null));

    assertEquals("42", result.getVenueId());
    assertEquals("Riverside Park", result.getVenueName());
    assertEquals("1 Main Street", result.getAddress());
    assertEquals(40.8, result.getLocation().getLatitude());
    assertEquals(-73.9, result.getLocation().getLongitude());
    assertEquals("America/New_York", result.getTimezone());
    assertEquals("Basketball", result.getSports().getFirst().getSport());
    assertEquals(new BigDecimal("20.00"), result.getSports().get(1).getPriceUsd());
    assertEquals("Hard court", result.getSports().get(1).getFacility().getSurfaceType());
    assertEquals(false, result.getSports().get(1).getFacility().isIndoor());
    assertEquals("MONDAY", result.getOpeningHours().getFirst().getDayOfWeek());
    assertEquals(LocalTime.of(14, 0), result.getOpeningHours().get(1).getOpensAt());
    assertEquals("SUNDAY", result.getOpeningHours().get(2).getDayOfWeek());
  }

  /**
   * Finds one venue by its trimmed name without changing the request.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void nameLookupUsesTrimmedExactName() {
    final VenueInfoRequest request = request(null, " Riverside Park ");
    when(venueRepository.findByNameIgnoreCase("Riverside Park"))
        .thenReturn(List.of(venue(42, "Riverside Park")));
    when(sportRepository.findByVenueId(42L)).thenReturn(List.of());
    when(hoursRepository.findByVenueId(42L)).thenReturn(List.of());

    final VenueInfo result = service.getVenueInfo(request);

    assertEquals("42", result.getVenueId());
    assertEquals(" Riverside Park ", request.getVenueName());
    assertEquals(List.of(), result.getSports());
    assertEquals(List.of(), result.getOpeningHours());
    verify(venueRepository).findByNameIgnoreCase("Riverside Park");
  }

  /**
   * Rejects missing, double, blank, and bad selectors before any data read.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void rejectsInvalidSelectors() {
    final List<VenueInfoRequest> requests =
        List.of(
            request(null, null),
            request("1", "Park"),
            request("", null),
            request("-1", null),
            request("abc", null),
            request("9223372036854775808", null),
            request(null, "  "));
    for (final VenueInfoRequest request : requests) {
      final VenueInfoException error =
          assertThrows(VenueInfoException.class, () -> service.getVenueInfo(request));
      assertEquals("request.validation_failed", error.getCode());
    }
    verifyNoInteractions(venueRepository, sportRepository, hoursRepository);
  }

  /**
   * Reports unknown IDs and names.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void reportsUnknownVenues() {
    when(venueRepository.findById(42L)).thenReturn(Optional.empty());
    when(venueRepository.findByNameIgnoreCase("Missing Park")).thenReturn(List.of());
    assertEquals(
        "venue.not_found",
        assertThrows(
                VenueInfoException.class, () -> service.getVenueInfo(request("42", null)))
            .getCode());
    assertEquals(
        "venue.not_found",
        assertThrows(
                VenueInfoException.class,
                () -> service.getVenueInfo(request(null, "Missing Park")))
            .getCode());
    verifyNoInteractions(sportRepository, hoursRepository);
  }

  /**
   * Reports an ambiguous name and reads no detail rows.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void reportsAmbiguousNames() {
    when(venueRepository.findByNameIgnoreCase("Central Park"))
        .thenReturn(List.of(venue(1, "Central Park"), venue(2, "Central Park")));
    final VenueInfoException error =
        assertThrows(
            VenueInfoException.class, () -> service.getVenueInfo(request(null, "Central Park")));
    assertEquals("venue.ambiguous_name", error.getCode());
    verifyNoInteractions(sportRepository, hoursRepository);
  }

  /**
   * Makes a venue info request for a test.
   *
   * @author Andrew Hsu, Codex
   */
  private VenueInfoRequest request(final String id, final String name) {
    final VenueInfoRequest request = new VenueInfoRequest();
    request.setVenueId(id);
    request.setVenueName(name);
    return request;
  }

  /**
   * Makes a venue with stored details for a test.
   *
   * @author Andrew Hsu, Codex
   */
  private Venue venue(final long id, final String name) {
    final Venue venue = new Venue();
    venue.setId(id);
    venue.setName(name);
    venue.setAddress("1 Main Street");
    venue.setLatitude(40.8);
    venue.setLongitude(-73.9);
    venue.setTimezone("America/New_York");
    return venue;
  }

  /**
   * Makes a venue sport with a price and facility values for a test.
   *
   * @author Andrew Hsu, Codex
   */
  private VenueSport sport(
      final Venue venue,
      final String name,
      final String price,
      final boolean isIndoor,
      final String surfaceType) {
    final Sport sport = new Sport();
    sport.setName(name);
    final VenueSport row = new VenueSport();
    row.setVenue(venue);
    row.setSport(sport);
    row.setPrice(new BigDecimal(price));
    row.setIsIndoor(isIndoor);
    row.setSurfaceType(surfaceType);
    return row;
  }

  /**
   * Makes one stored opening period for a test.
   *
   * @author Andrew Hsu, Codex
   */
  private OpeningInterval hours(
      final Venue venue, final String day, final String opens, final String closes) {
    final OpeningInterval row = new OpeningInterval();
    row.setVenue(venue);
    row.setDayOfWeek(DayOfWeek.valueOf(day));
    row.setOpensAt(LocalTime.parse(opens));
    row.setClosesAt(LocalTime.parse(closes));
    return row;
  }
}
