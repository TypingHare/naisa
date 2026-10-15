package com.naisa.controller.v1;

import static com.naisa.VenueSearchTestData.data;
import static com.naisa.VenueSearchTestData.venue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.naisa.config.WebConfiguration;
import com.naisa.controller.GlobalExceptionHandler;
import com.naisa.exception.ClientException;
import com.naisa.interceptor.AuthorizationInterceptor;
import com.naisa.interceptor.LoggingInterceptor;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.entity.Client;
import com.naisa.model.vo.FacilityInfo;
import com.naisa.repository.VenueSearchRepository;
import com.naisa.service.ClientService;
import com.naisa.service.VenueInfoService;
import com.naisa.service.VenueSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Checks JSON input, authentication, and complete search responses without a database.
 *
 * @author Andrew Hsu, Codex
 */
@WebMvcTest(VenueSearchController.class)
@ContextConfiguration(
    classes = {
      VenueSearchController.class,
      VenueSearchService.class,
      GlobalExceptionHandler.class,
      WebConfiguration.class,
      LoggingInterceptor.class,
      AuthorizationInterceptor.class
    })
class VenueSearchControllerTest {
  private static final String SEARCH_BODY =
      """
      {
        "location": {"latitude": 0, "longitude": 0},
        "sport": "basketball",
        "date": "2026-10-17",
        "time": "18:00",
        "maxDistance": 5
      }
      """;
  private static final String GROUP_BODY =
      """
      {
        "locations": [{"latitude": 0, "longitude": 0},
                      {"latitude": 0, "longitude": 0.01}],
        "sport": "basketball",
        "date": "2026-10-17",
        "time": "18:00"
      }
      """;

  @Autowired private MockMvc mvc;
  @MockitoBean private VenueSearchRepository repository;
  @MockitoBean private ClientService clientService;
  @MockitoBean private VenueInfoService venueInfoService;

  /**
   * Returns known facility values with the intended JSON names.
   *
   * @author Andrew Hsu
   */
  @Test
  void knownFacilityFieldsPreserveTheirJsonNames() throws Exception {
    final VenueSearchData data = data(venue(1, 0, "0"));
    final FacilityInfo facility = new FacilityInfo();
    facility.setIsIndoor(false);
    facility.setHasLighting(true);
    data.getVenues().getFirst().setFacility(facility);
    when(repository.findBySportName("basketball")).thenReturn(data);
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.searchLimitReached").value(false))
        .andExpect(jsonPath("$.data.venues[0].availability.isOpen").value(true))
        .andExpect(jsonPath("$.data.venues[0].availability.open").doesNotExist())
        .andExpect(jsonPath("$.data.venues[0].facility.isIndoor").value(false))
        .andExpect(jsonPath("$.data.venues[0].facility.indoor").doesNotExist())
        .andExpect(jsonPath("$.data.venues[0].facility.hasLighting").value(true));
  }

  /**
   * Set up.
   *
   * @author Andrew Hsu
   */
  @BeforeEach
  void setUp() {
    when(clientService.getClientByApiKey("test-key")).thenReturn(new Client());
    when(repository.findBySportName("basketball")).thenReturn(data(venue(1, 0, "0")));
  }

  /**
   * Exact search returns the capsule and default budget.
   *
   * @author Andrew Hsu
   */
  @Test
  void exactSearchReturnsTheCapsuleAndDefaultBudget() throws Exception {
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").doesNotExist())
        .andExpect(jsonPath("$.data.matchType").value("EXACT"))
        .andExpect(jsonPath("$.data.requestedCriteria.budget").value(0))
        .andExpect(jsonPath("$.data.venues[0].venueId").value("1"))
        .andExpect(jsonPath("$.data.venues[0].availability.bookingStatus").value("UNKNOWN"))
        .andExpect(jsonPath("$.data.venues[0].availability.time").value("18:00"))
        .andExpect(jsonPath("$.data.venues[0].facility").doesNotExist());
  }

  /**
   * Venue search raises the budget when no exact venue matches.
   *
   * @author Andrew Hsu
   */
  @Test
  void venueSearchSuggestsAlternatives() throws Exception {
    when(repository.findBySportName("basketball")).thenReturn(data(venue(1, 0, "10")));
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.matchType").value("ALTERNATIVE"))
        .andExpect(jsonPath("$.data.venues", hasSize(1)))
        .andExpect(jsonPath("$.data.effectiveCriteria.budget").value(10))
        .andExpect(jsonPath("$.data.relaxedFields[0]").value("maxDistance"))
        .andExpect(jsonPath("$.data.relaxedFields[1]").value("budget"))
        .andExpect(jsonPath("$.data.searchLimitReached").value(false));
  }

  /**
   * Group search returns distances in input order.
   *
   * @author Andrew Hsu
   */
  @Test
  void groupSearchReturnsDistancesInInputOrder() throws Exception {
    mvc.perform(
            post("/v1/venues/group-search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(GROUP_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.matchType").value("EXACT"))
        .andExpect(jsonPath("$.data.center").exists())
        .andExpect(jsonPath("$.data.venues[0].memberDistancesKm", hasSize(2)))
        .andExpect(jsonPath("$.data.venues[0].memberDistancesKm[0]").value(0))
        .andExpect(jsonPath("$.data.venues[0].averageDistanceKm").exists())
        .andExpect(jsonPath("$.data.venues[0].distanceKm").doesNotExist());
  }

  /**
   * Both search routes require an API key.
   *
   * @author Andrew Hsu
   */
  @ParameterizedTest
  @ValueSource(strings = {"/search", "/group-search"})
  void everyEndpointRequiresAnApiKey(final String path) throws Exception {
    mvc.perform(post("/v1/venues" + path).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value("authorization.no_api_key_provided"));
    verifyNoInteractions(repository);
  }

  /**
   * Rejects an unissued api key.
   *
   * @author Andrew Hsu
   */
  @Test
  void rejectsAnUnissuedApiKey() throws Exception {
    when(clientService.getClientByApiKey("bad-key"))
        .thenThrow(ClientException.INVALID_API_KEY.clone());
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer bad-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value("client.invalid_api_key"));
    verifyNoInteractions(repository);
  }

  /**
   * Rejects missing or invalid coordinates.
   *
   * @author Andrew Hsu
   */
  @ParameterizedTest
  @ValueSource(
      strings = {"{}", "{\"location\":null}", "{\"location\":{\"latitude\":91,\"longitude\":0}}"})
  void rejectsMissingOrInvalidCoordinates(final String body) throws Exception {
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("request.validation_failed"));
    verifyNoInteractions(repository);
  }

  /**
   * Rejects invalid search radii.
   *
   * @author Andrew Hsu
   */
  @ParameterizedTest
  @ValueSource(strings = {"-1", "\"Infinity\"", "\"NaN\""})
  void rejectsInvalidSearchRadii(final String radius) throws Exception {
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY.replace("\"maxDistance\": 5", "\"maxDistance\": " + radius)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(repository);
  }

  /**
   * Rejects invalid budgets.
   *
   * @author Andrew Hsu
   */
  @ParameterizedTest
  @ValueSource(strings = {"-0.01", "0.001", "null"})
  void rejectsInvalidBudgets(final String budget) throws Exception {
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    SEARCH_BODY.replace(
                        "\"maxDistance\": 5", "\"maxDistance\": 5, \"budget\": " + budget)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(repository);
  }

  /**
   * Rejects invalid dates and malformed json.
   *
   * @author Andrew Hsu
   */
  @Test
  void rejectsInvalidDatesAndMalformedJson() throws Exception {
    for (final String body :
        new String[] {
          "{",
          SEARCH_BODY.replace("2026-10-17", "2026-02-30"),
          SEARCH_BODY.replace("18:00", "25:00")
        }) {
      mvc.perform(
              post("/v1/venues/search")
                  .header("Authorization", "Bearer test-key")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errorCode").value("request.invalid_body"));
    }
    verifyNoInteractions(repository);
  }

  /**
   * Rejects null group members and undefined centers.
   *
   * @author Andrew Hsu
   */
  @Test
  void rejectsNullGroupMembersAndUndefinedCenters() throws Exception {
    for (final String body :
        new String[] {
          GROUP_BODY.replace("{\"latitude\": 0, \"longitude\": 0.01}", "null"),
          GROUP_BODY.replace("\"longitude\": 0.01", "\"longitude\": 180")
        }) {
      mvc.perform(
              post("/v1/venues/group-search")
                  .header("Authorization", "Bearer test-key")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isBadRequest());
    }
    verifyNoInteractions(repository);
  }

  /**
   * Unfinished data source is not an empty search.
   *
   * @author Andrew Hsu
   */
  @ParameterizedTest
  @ValueSource(strings = {"/search", "/group-search"})
  void unfinishedDataSourceIsNotAnEmptySearch(final String path) throws Exception {
    when(repository.findBySportName("basketball")).thenReturn(new VenueSearchData());
    final String body = "/group-search".equals(path) ? GROUP_BODY : SEARCH_BODY;
    mvc.perform(
            post("/v1/venues" + path)
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotImplemented())
        .andExpect(jsonPath("$.errorCode").value("search.data_source_not_implemented"));
  }

  /**
   * Unknown sports cause an error, while a search with no venues returns no matches.
   *
   * @author Andrew Hsu
   */
  @Test
  void unknownSportAndNoNearbyVenuesHaveDifferentResponses() throws Exception {
    final VenueSearchData unknown = data();
    unknown.setSportFound(false);
    when(repository.findBySportName("basketball")).thenReturn(unknown);
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errorCode").value("search.unknown_sport"));
    when(repository.findBySportName("basketball")).thenReturn(data());
    mvc.perform(
            post("/v1/venues/search")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SEARCH_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.matchType").value("NONE"))
        .andExpect(jsonPath("$.data.venues", hasSize(0)));
  }
}
