package com.naisa.repository;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.naisa.model.data.VenueCandidate;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks PostgreSQL reads and HTTP search results with transaction rollback after each test.
 *
 * @author Andrew Hsu, Codex
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
class PostgresVenueSearchRepositoryTest {
  @Autowired private VenueSearchRepository repository;
  @Autowired private EntityManager entityManager;
  @Autowired private MockMvc mvc;

  /**
   * Reads complete candidates in three queries even with multiple venues and opening periods.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void readsJoinedDataAndLoadsHoursInBulk() {
    final Sport sport = sport();
    final VenueSport first = venueSport(sport, "10.00", "USD", "PER_COURT_HOUR");
    first.setSurfaceType("Hard court");
    period(first.getVenue(), DayOfWeek.MONDAY, "14:00", "17:00");
    period(first.getVenue(), DayOfWeek.SUNDAY, "22:00", "02:00");
    final VenueSport second = venueSport(sport, "20.00", null, null);
    final VenueSport third = venueSport(sport, "30.00", "EUR", "PER_PERSON");
    entityManager.flush();
    entityManager.clear();
    final Statistics statistics =
        entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    final long before = statistics.getPrepareStatementCount();

    final VenueSearchData result =
        repository.findBySportName(sport.getName().toUpperCase(Locale.ROOT));

    assertEquals(3L, statistics.getPrepareStatementCount() - before);
    assertTrue(result.isSportFound());
    assertEquals(3, result.getVenues().size());
    final VenueCandidate candidate = result.getVenues().getFirst();
    assertEquals(first.getVenue().getId(), candidate.getVenueId());
    assertEquals("Test venue", candidate.getVenueName());
    assertEquals(sport.getName(), candidate.getSport());
    assertEquals(0.0, candidate.getLocation().getLatitude());
    assertEquals(0.0, candidate.getLocation().getLongitude());
    assertEquals("UTC", candidate.getTimezone());
    assertEquals(new BigDecimal("10.00"), candidate.getPrice());
    assertEquals("USD", candidate.getCurrency());
    assertEquals("PER_COURT_HOUR", candidate.getPriceUnit());
    assertFalse(candidate.getFacility().isIndoor());
    assertEquals("Hard court", candidate.getFacility().getSurfaceType());
    assertEquals(
        List.of(1, 1, 7),
        candidate.getOpeningHours().stream()
            .map(com.naisa.model.data.OpeningInterval::getDayOfWeek).sorted().toList());
    assertTrue(
        candidate.getOpeningHours().stream()
            .anyMatch(row -> row.getDayOfWeek() == 7
                && row.getClosesAt().equals(LocalTime.of(2, 0))));
    assertEquals(second.getVenue().getId(), result.getVenues().get(1).getVenueId());
    assertNull(result.getVenues().get(1).getCurrency());
    assertNull(result.getVenues().get(1).getPriceUnit());
    assertEquals(third.getVenue().getId(), result.getVenues().get(2).getVenueId());
    assertEquals("EUR", result.getVenues().get(2).getCurrency());
    assertEquals("PER_PERSON", result.getVenues().get(2).getPriceUnit());
  }

  /**
   * Keeps an unknown sport distinct from a stored sport with no venue records.
   *
   * @author Andrew Hsu, Codex
   */
  @Test
  void distinguishesUnknownSportFromEmptyStoredSport() {
    final Sport sport = sport();
    entityManager.flush();
    entityManager.clear();
    final VenueSearchData stored = repository.findBySportName(sport.getName());
    assertTrue(stored.isSportFound());
    assertTrue(stored.getVenues().isEmpty());
    final VenueSearchData missing = repository.findBySportName("missing-" + UUID.randomUUID());
    assertFalse(missing.isSportFound());
    assertTrue(missing.getVenues().isEmpty());
  }

  /**
   * Both routes return one offer per venue with supported currency and units.
   *
   * @param path the search route
   * @throws Exception if the HTTP check fails
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @ValueSource(strings = {"/search", "/group-search"})
  void routesReturnOnlyComparableStoredPrices(final String path) throws Exception {
    final Sport sport = sport();
    final VenueSport supported = venueSport(sport, "10.00", "USD", "PER_COURT_HOUR");
    venueSport(sport, "0.00", null, "PER_COURT_HOUR");
    venueSport(sport, "0.00", "USD", null);
    venueSport(sport, "0.00", "EUR", "PER_COURT_HOUR");
    venueSport(sport, "0.00", "USD", "PER_PERSON");
    final Sport sameName = new Sport();
    sameName.setName(sport.getName().toLowerCase(Locale.ROOT));
    entityManager.persist(sameName);
    final VenueSport cheaper = new VenueSport();
    cheaper.setVenue(supported.getVenue());
    cheaper.setSport(sameName);
    cheaper.setPrice(new BigDecimal("5.00"));
    cheaper.setCurrency("USD");
    cheaper.setPriceUnit("PER_COURT_HOUR");
    cheaper.setIsIndoor(false);
    entityManager.persist(cheaper);
    final Client client = new Client();
    entityManager.persist(client);
    entityManager.flush();
    entityManager.clear();

    mvc.perform(
            post("/v1/venues" + path)
                .header("Authorization", "Bearer " + client.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(path, sport.getName(), 10)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.matchType").value("EXACT"))
        .andExpect(jsonPath("$.data.venues", hasSize(1)))
        .andExpect(jsonPath("$.data.venues[0].venueId").value(supported.getVenue().getId().toString()))
        .andExpect(jsonPath("$.data.venues[0].estimatedPrice.amount").value(5))
        .andExpect(jsonPath("$.data.venues[0].estimatedPrice.currency").value("USD"))
        .andExpect(jsonPath("$.data.venues[0].estimatedPrice.priceUnit").value("PER_COURT_HOUR"));
  }

  /**
   * Unclassified prices produce a normal empty result instead of an unsupported-source error.
   *
   * @param path the search route
   * @throws Exception if the HTTP check fails
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @ValueSource(strings = {"/search", "/group-search"})
  void routesReturnEmptyResultsForUnclassifiedPrices(final String path) throws Exception {
    final Sport sport = sport();
    venueSport(sport, "0.00", null, null);
    final Client client = new Client();
    entityManager.persist(client);
    entityManager.flush();
    entityManager.clear();
    mvc.perform(
            post("/v1/venues" + path)
                .header("Authorization", "Bearer " + client.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(path, sport.getName(), 0)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").doesNotExist())
        .andExpect(jsonPath("$.data.matchType").value("NONE"))
        .andExpect(jsonPath("$.data.venues", hasSize(0)));
  }

  /**
   * Makes a unique sport name so existing data cannot affect a test.
   *
   * @return the stored sport
   * @author Andrew Hsu, Codex
   */
  private Sport sport() {
    final Sport sport = new Sport();
    sport.setName("Test-" + UUID.randomUUID().toString().substring(0, 16));
    entityManager.persist(sport);
    return sport;
  }

  /**
   * Stores a venue sport with a Monday opening period.
   *
   * @param sport the stored sport
   * @param price the stored amount
   * @param currency the currency, or null when unknown
   * @param unit the price unit, or null when unknown
   * @return the stored venue sport
   * @author Andrew Hsu, Codex
   */
  private VenueSport venueSport(
      final Sport sport, final String price, final String currency, final String unit) {
    final Venue venue = new Venue();
    venue.setName("Test venue");
    venue.setAddress("1 Test Street");
    venue.setLatitude(0.0);
    venue.setLongitude(0.0);
    venue.setTimezone("UTC");
    entityManager.persist(venue);
    final VenueSport row = new VenueSport();
    row.setVenue(venue);
    row.setSport(sport);
    row.setPrice(new BigDecimal(price));
    row.setCurrency(currency);
    row.setPriceUnit(unit);
    row.setIsIndoor(false);
    entityManager.persist(row);
    period(venue, DayOfWeek.MONDAY, "09:00", "22:00");
    return row;
  }

  /**
   * Stores one opening period.
   *
   * @param venue the stored venue
   * @param day the weekday
   * @param opens the opening time
   * @param closes the closing time
   * @author Andrew Hsu, Codex
   */
  private void period(
      final Venue venue, final DayOfWeek day, final String opens, final String closes) {
    final OpeningInterval period = new OpeningInterval();
    period.setVenue(venue);
    period.setDayOfWeek(day);
    period.setOpensAt(LocalTime.parse(opens));
    period.setClosesAt(LocalTime.parse(closes));
    entityManager.persist(period);
  }

  /**
   * Builds a valid request at the stored venue's location and local opening time.
   *
   * @param path the search route
   * @param sport the sport name
   * @param budget the requested budget
   * @return the JSON request
   * @author Andrew Hsu, Codex
   */
  private String body(final String path, final String sport, final int budget) {
    final String locations =
        "/search".equals(path)
            ? "\"location\": {\"latitude\": 0, \"longitude\": 0}, \"maxDistance\": 5"
            : "\"locations\": [{\"latitude\": 0, \"longitude\": 0},"
                + " {\"latitude\": 0, \"longitude\": 0.01}]";
    return """
        {%s, "sport": "%s", "date": "2026-10-19", "time": "10:00", "budget": %d}
        """.formatted(locations, sport, budget);
  }
}
