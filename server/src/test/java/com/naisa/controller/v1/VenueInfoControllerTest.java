package com.naisa.controller.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.naisa.config.WebConfiguration;
import com.naisa.controller.GlobalExceptionHandler;
import com.naisa.exception.VenueInfoException;
import com.naisa.interceptor.AuthorizationInterceptor;
import com.naisa.interceptor.LoggingInterceptor;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.VenueInfoRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.vo.FacilityInfo;
import com.naisa.model.vo.VenueInfo;
import com.naisa.model.vo.VenueOpeningHours;
import com.naisa.model.vo.VenueSportInfo;
import com.naisa.service.ClientService;
import com.naisa.service.VenueInfoService;
import com.naisa.service.VenueSearchService;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Checks the venue info route and its JSON response.
 *
 * @author Andrew Hsu, Codex
 */
@WebMvcTest(VenueSearchController.class)
@ContextConfiguration(
    classes = {
      VenueSearchController.class,
      GlobalExceptionHandler.class,
      WebConfiguration.class,
      LoggingInterceptor.class,
      AuthorizationInterceptor.class
    })
class VenueInfoControllerTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private VenueSearchService searchService;
  @MockitoBean private VenueInfoService infoService;
  @MockitoBean private ClientService clientService;

  /**
   * Sets up a valid API key.
   *
   * @author Andrew Hsu
   */
  @BeforeEach
  void setUp() {
    when(clientService.getClientByApiKey("test-key")).thenReturn(new Client());
  }

  /**
   * Returns one venue inside a capsule.
   *
   * @author Andrew Hsu
   */
  @Test
  void getsVenueInfoById() throws Exception {
    final VenueInfo info = new VenueInfo();
    info.setVenueId("42");
    info.setVenueName("Riverside Park");
    info.setAddress("1 Main Street");
    info.setTimezone("America/New_York");
    final Location location = new Location();
    location.setLatitude(40.8);
    location.setLongitude(-73.9);
    info.setLocation(location);
    final FacilityInfo facility = new FacilityInfo();
    facility.setIsIndoor(false);
    facility.setSurfaceType("Hard court");
    final VenueSportInfo sport = new VenueSportInfo();
    sport.setSport("Tennis");
    sport.setPriceUsd(new BigDecimal("20.00"));
    sport.setFacility(facility);
    info.setSports(List.of(sport));
    final VenueOpeningHours hours = new VenueOpeningHours();
    hours.setDayOfWeek("MONDAY");
    hours.setOpensAt(LocalTime.of(9, 0));
    hours.setClosesAt(LocalTime.of(22, 0));
    info.setOpeningHours(List.of(hours));
    when(infoService.getVenueInfo(any(VenueInfoRequest.class))).thenReturn(info);

    mvc.perform(
            post("/v1/venues/info")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":\"42\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").doesNotExist())
        .andExpect(jsonPath("$.data.venueId").value("42"))
        .andExpect(jsonPath("$.data.venueName").value("Riverside Park"))
        .andExpect(jsonPath("$.data.address").value("1 Main Street"))
        .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
        .andExpect(jsonPath("$.data.location.latitude").value(40.8))
        .andExpect(jsonPath("$.data.sports[0].sport").value("Tennis"))
        .andExpect(jsonPath("$.data.sports[0].priceUsd").value(20.00))
        .andExpect(jsonPath("$.data.sports[0].facility.surfaceType").value("Hard court"))
        .andExpect(jsonPath("$.data.sports[0].facility.isIndoor").value(false))
        .andExpect(jsonPath("$.data.openingHours[0].dayOfWeek").value("MONDAY"))
        .andExpect(jsonPath("$.data.openingHours[0].opensAt").value("09:00"))
        .andExpect(jsonPath("$.data.openingHours[0].closesAt").value("22:00"));
  }

  /**
   * Checks the API key before the lookup.
   *
   * @author Andrew Hsu
   */
  @Test
  void requiresApiKey() throws Exception {
    mvc.perform(
            post("/v1/venues/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":\"42\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value("authorization.no_api_key_provided"));
    verifyNoInteractions(infoService);
  }

  /**
   * Maps lookup errors to the HTTP response.
   *
   * @author Andrew Hsu
   */
  @Test
  void returnsLookupErrors() throws Exception {
    when(infoService.getVenueInfo(any(VenueInfoRequest.class)))
        .thenThrow(VenueInfoException.NOT_FOUND.clone());
    mvc.perform(
            post("/v1/venues/info")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":\"42\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("venue.not_found"));
  }

  /**
   * Maps a bad selector and an ambiguous name to distinct errors.
   *
   * @author Andrew Hsu
   */
  @Test
  void returnsSelectorErrors() throws Exception {
    when(infoService.getVenueInfo(any(VenueInfoRequest.class)))
        .thenThrow(VenueInfoException.INVALID_SELECTOR.clone())
        .thenThrow(VenueInfoException.AMBIGUOUS_NAME.clone());
    mvc.perform(
            post("/v1/venues/info")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueId\":\"42\",\"venueName\":\"Park\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("request.validation_failed"));
    mvc.perform(
            post("/v1/venues/info")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"venueName\":\"Park\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("venue.ambiguous_name"));
  }

  /**
   * Reports a missing or bad JSON body.
   *
   * @author Andrew Hsu
   */
  @Test
  void rejectsBadJson() throws Exception {
    mvc.perform(
            post("/v1/venues/info")
                .header("Authorization", "Bearer test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("request.invalid_body"));
    verifyNoInteractions(infoService);
  }
}
