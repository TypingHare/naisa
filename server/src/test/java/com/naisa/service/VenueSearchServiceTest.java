package com.naisa.service;

import static com.naisa.VenueSearchTestData.data;
import static com.naisa.VenueSearchTestData.hours;
import static com.naisa.VenueSearchTestData.location;
import static com.naisa.VenueSearchTestData.request;
import static com.naisa.VenueSearchTestData.venue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueSearchException;
import com.naisa.model.data.VenueCandidate;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.dto.GroupSearchRequest;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.vo.SearchResult;
import com.naisa.model.vo.VenueResult;
import com.naisa.repository.PlaceholderVenueSearchRepository;
import com.naisa.repository.VenueSearchRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Checks the search rules with test-only venue records.
 *
 * @author Andrew Hsu
 */
@ExtendWith(MockitoExtension.class)
class VenueSearchServiceTest {
  @Mock private VenueSearchRepository repository;
  private VenueSearchService service;

  /**
   * Set up.
   *
   * @author Andrew Hsu
   */
  @BeforeEach
  void setUp() {
    service = new VenueSearchService(repository);
  }

  /**
   * Exact matches keep criteria and use unrounded ordering.
   *
   * @author Andrew Hsu
   */
  @Test
  void exactMatchesKeepCriteriaAndUseUnroundedOrdering() {
    final VenueCandidate farther = venue(1, 0.0000011, "0");
    final VenueCandidate closer = venue(2, 0.0000010, "0");
    when(repository.findBySportName("basketball")).thenReturn(data(farther, closer));
    final VenueSearchRequest request = request();
    request.setSport(" basketball ");
    final SearchResult result = service.findVenues(request);
    assertEquals("EXACT", result.getMatchType());
    assertEquals(
        List.of("2", "1"), result.getVenues().stream().map(VenueResult::getVenueId).toList());
    assertTrue(result.getRelaxedFields().isEmpty());
    assertEquals("UNKNOWN", result.getVenues().getFirst().getAvailability().getBookingStatus());
    assertEquals(" basketball ", request.getSport());
    verify(repository).findBySportName("basketball");
  }

  /**
   * The exact distance and budget limits include their boundary values.
   *
   * @author Andrew Hsu
   */
  @Test
  void acceptsExactRadiusAndBudgetBoundaries() {
    final VenueCandidate candidate = venue(1, 0.01, "0.30");
    when(repository.findBySportName("basketball")).thenReturn(data(candidate));
    final VenueSearchRequest request = request();
    request.setMaxDistance(SearchGeometry.distance(request.getLocation(), candidate.getLocation()));
    request.setBudget(new BigDecimal("0.30"));
    assertEquals("EXACT", service.findVenues(request).getMatchType());
    request.setBudget(new BigDecimal("0.29"));
    assertEquals("ALTERNATIVE", service.findVenues(request).getMatchType());
    request.setBudget(new BigDecimal("0.30"));
    request.setMaxDistance(request.getMaxDistance() - 0.0000001);
    assertEquals("ALTERNATIVE", service.findVenues(request).getMatchType());
  }

  /**
   * Opening is inclusive and closing is exclusive, even during fallback.
   *
   * @author Andrew Hsu
   */
  @Test
  void openingIsInclusiveAndClosingIsExclusive() {
    final VenueCandidate candidate = venue(1, 0, "0");
    candidate.setOpeningHours(List.of(hours(6, "09:00", "22:00")));
    when(repository.findBySportName("basketball")).thenReturn(data(candidate));
    final VenueSearchRequest request = request();
    request.setTime(LocalTime.of(9, 0));
    assertEquals("EXACT", service.findVenues(request).getMatchType());
    request.setTime(LocalTime.of(22, 0));
    assertEquals("NONE", service.findVenues(request).getMatchType());
  }

  /**
   * Overnight hours carry from Sunday into Monday.
   *
   * @author Andrew Hsu
   */
  @Test
  void overnightHoursCarryFromSundayIntoMonday() {
    final VenueCandidate candidate = venue(1, 0, "0");
    candidate.setOpeningHours(List.of(hours(7, "22:00", "02:00")));
    when(repository.findBySportName("basketball")).thenReturn(data(candidate));
    final VenueSearchRequest request = request();
    request.setDate(LocalDate.of(2026, 10, 19));
    request.setTime(LocalTime.of(1, 59));
    assertEquals("EXACT", service.findVenues(request).getMatchType());
    request.setTime(LocalTime.of(2, 0));
    assertEquals("NONE", service.findVenues(request).getMatchType());
  }

  /**
   * Excludes times that do not exist in the venue time zone.
   *
   * @author Andrew Hsu
   */
  @Test
  void excludesTimesThatDoNotExistInTheVenueTimeZone() {
    final VenueCandidate candidate = venue(1, 0, "0");
    candidate.setTimezone("America/New_York");
    candidate.setOpeningHours(List.of(hours(7, "00:00", "04:00")));
    when(repository.findBySportName("basketball")).thenReturn(data(candidate));
    final VenueSearchRequest request = request();
    request.setDate(LocalDate.of(2026, 3, 8));
    request.setTime(LocalTime.of(2, 30));
    assertEquals("NONE", service.findVenues(request).getMatchType());
  }

  /**
   * Unknown prices, units, hours, and zones are not matches.
   *
   * @author Andrew Hsu
   */
  @Test
  void unknownPricesUnitsHoursAndZonesAreNotMatches() {
    final VenueCandidate unknownPrice = venue(1, 0, "0");
    unknownPrice.setPrice(null);
    final VenueCandidate otherUnit = venue(2, 0, "0");
    otherUnit.setPriceUnit("PER_PERSON");
    final VenueCandidate unknownHours = venue(3, 0, "0");
    unknownHours.setOpeningHours(List.of());
    final VenueCandidate unknownZone = venue(4, 0, "0");
    unknownZone.setTimezone("Invalid/Zone");
    when(repository.findBySportName("basketball"))
        .thenReturn(data(unknownPrice, otherUnit, unknownHours, unknownZone));
    assertEquals("NONE", service.findVenues(request()).getMatchType());
  }

  /**
   * The merged search raises both limits by five per round and keeps the input unchanged.
   *
   * @author Andrew Hsu
   */
  @Test
  void fallbackRaisesBudgetAndDistanceTogether() {
    when(repository.findBySportName("basketball")).thenReturn(data(venue(1, 0.08, "10")));
    final VenueSearchRequest request = request();
    final SearchResult result = service.findVenues(request);
    assertEquals("ALTERNATIVE", result.getMatchType());
    assertEquals(List.of("maxDistance", "budget"), result.getRelaxedFields());
    final VenueSearchRequest effective = (VenueSearchRequest) result.getEffectiveCriteria();
    assertEquals(15.0, effective.getMaxDistance());
    assertEquals(new BigDecimal("10.00"), effective.getBudget());
    assertEquals(5.0, request.getMaxDistance());
    assertEquals(BigDecimal.ZERO, result.getRequestedCriteria().getBudget());
    assertEquals(request.getDate(), effective.getDate());
    assertEquals(request.getTime(), effective.getTime());
    assertEquals(1, result.getVenues().size());
    verify(repository).findBySportName("basketball");
  }

  /**
   * The first successful round returns all venues within that round's limits.
   *
   * @author Andrew Hsu
   */
  @Test
  void firstSuccessfulRoundReturnsAllMatches() {
    when(repository.findBySportName("basketball"))
        .thenReturn(data(venue(1, 0, "5"), venue(2, 0.08, "0"), venue(3, 0, "10")));
    final SearchResult result = service.findVenues(request());
    assertEquals("ALTERNATIVE", result.getMatchType());
    assertEquals(
        List.of("1", "2"), result.getVenues().stream().map(VenueResult::getVenueId).toList());
    final VenueSearchRequest effective = (VenueSearchRequest) result.getEffectiveCriteria();
    assertEquals(10.0, effective.getMaxDistance());
    assertEquals(new BigDecimal("5.00"), effective.getBudget());
  }

  /**
   * The search stops after three failed rounds and keeps the date and time fixed.
   *
   * @author Andrew Hsu
   */
  @Test
  void stopsAtTheRelaxationLimit() {
    final VenueCandidate expensive = venue(1, 0, "20");
    final VenueCandidate far = venue(2, 0.2, "0");
    final VenueCandidate closed = venue(3, 0, "0");
    closed.setOpeningHours(List.of(hours(7, "09:00", "22:00")));
    when(repository.findBySportName("basketball")).thenReturn(data(expensive, far, closed));
    final SearchResult result = service.findVenues(request());
    assertEquals("NONE", result.getMatchType());
    assertTrue(result.getVenues().isEmpty());
    assertTrue(result.isSearchLimitReached());
    assertTrue(result.getRelaxedFields().isEmpty());
    assertEquals(null, result.getEffectiveCriteria());
  }

  /**
   * Empty sport data has no matches, while an unknown sport causes an error.
   *
   * @author Andrew Hsu
   */
  @Test
  void emptySportDataAndUnknownSportHaveDifferentResults() {
    when(repository.findBySportName("basketball")).thenReturn(data());
    assertEquals("NONE", service.findVenues(request()).getMatchType());
    final VenueSearchData unknown = data();
    unknown.setSportFound(false);
    when(repository.findBySportName("basketball")).thenReturn(unknown);
    assertEquals(
        "search.unknown_sport",
        assertThrows(VenueSearchException.class, () -> service.findVenues(request())).getCode());
  }

  /**
   * Placeholder does not claim that the database has no venues.
   *
   * @author Andrew Hsu
   */
  @Test
  void placeholderDoesNotClaimThatTheDatabaseHasNoVenues() {
    final VenueSearchService placeholder =
        new VenueSearchService(new PlaceholderVenueSearchRepository());
    assertEquals(
        "search.data_source_not_implemented",
        assertThrows(VenueSearchException.class, () -> placeholder.findVenues(request()))
            .getCode());
  }

  /**
   * Group distances preserve member order and duplicate members.
   *
   * @author Andrew Hsu
   */
  @Test
  void groupDistancesPreserveMemberOrderAndDuplicateMembers() {
    when(repository.findBySportName("basketball")).thenReturn(data(venue(1, 0, "0")));
    final GroupSearchRequest request = groupRequest();
    request.setLocations(List.of(location(0, 0), location(0, 2), location(0, 0)));
    final SearchResult result = service.findGroupVenues(request);
    final VenueResult match = result.getVenues().getFirst();
    assertEquals(3, match.getMemberDistancesKm().size());
    assertEquals(0.0, match.getMemberDistancesKm().getFirst());
    assertEquals(222.390, match.getMemberDistancesKm().get(1), 0.001);
    assertEquals(0.0, match.getMemberDistancesKm().getLast());
    assertEquals(74.130, match.getAverageDistanceKm(), 0.001);
    assertNotEquals(match.getAverageDistanceKm(), match.getDistanceFromCenterKm());
    request.setDistanceFromCenter(5.0);
    final SearchResult outside = service.findGroupVenues(request);
    assertEquals("NONE", outside.getMatchType());
    assertFalse(outside.isSearchLimitReached());
  }

  /**
   * Group results rank by average distance rather than center distance.
   *
   * @author Andrew Hsu
   */
  @Test
  void groupResultsRankByAverageDistanceRatherThanCenterDistance() {
    when(repository.findBySportName("basketball"))
        .thenReturn(data(venue(1, 0, "0"), venue(2, 0.5, "0")));
    final GroupSearchRequest request = groupRequest();
    request.setLocations(List.of(location(0, 0), location(0, 0), location(0, 1.5)));
    assertEquals("1", service.findGroupVenues(request).getVenues().getFirst().getVenueId());
  }

  /**
   * Group center handles the longitude boundary and rejects antipodes.
   *
   * @author Andrew Hsu
   */
  @Test
  void groupCenterHandlesTheLongitudeBoundaryAndRejectsAntipodes() {
    final Location center = SearchGeometry.center(List.of(location(0, 179), location(0, -179)));
    assertEquals(180, Math.abs(center.getLongitude()), 0.001);
    assertEquals(222.390, SearchGeometry.distance(location(0, 179), location(0, -179)), 0.001);
    assertThrows(
        VenueSearchException.class,
        () -> SearchGeometry.center(List.of(location(0, 0), location(0, 180))));
  }

  /**
   * Group request.
   *
   * @author Andrew Hsu
   */
  private GroupSearchRequest groupRequest() {
    final GroupSearchRequest request = new GroupSearchRequest();
    request.setSport("basketball");
    request.setDate(LocalDate.of(2026, 10, 17));
    request.setTime(LocalTime.of(18, 0));
    return request;
  }
}
