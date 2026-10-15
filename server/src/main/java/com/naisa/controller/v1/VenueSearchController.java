package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.model.dto.GroupSearchRequest;
import com.naisa.model.dto.VenueInfoRequest;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.vo.SearchResult;
import com.naisa.model.vo.VenueInfo;
import com.naisa.service.VenueInfoService;
import com.naisa.service.VenueSearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes venue search and info operations.
 *
 * @author Andrew Hsu
 */
@RestController
@RequestMapping("/v1/venues")
public class VenueSearchController {
  private final VenueSearchService service;
  private final VenueInfoService infoService;

  /**
   * Connects the routes to the search and info services.
   *
   * @param service the search service
   * @param infoService the venue info service
   * @author Andrew Hsu
   */
  public VenueSearchController(
      final VenueSearchService service, final VenueInfoService infoService) {
    this.service = service;
    this.infoService = infoService;
  }

  /**
   * Runs proposal Operations 1 and 2 as one search with bounded alternatives.
   *
   * @param request the search values
   * @return the search result in a capsule
   * @author Andrew Hsu
   */
  @PostMapping("/search")
  @AuthorizationRequired
  public ResponseEntity<Capsule<SearchResult>> findVenues(
      @Valid @RequestBody final VenueSearchRequest request) {
    return ResponseEntity.ok(new Capsule<>("Venue search completed.", service.findVenues(request)));
  }

  /**
   * Runs proposal Operation 3 and returns each member's distance.
   *
   * @param request the group search values
   * @return the group search result in a capsule
   * @author Andrew Hsu
   */
  @PostMapping("/group-search")
  @AuthorizationRequired
  public ResponseEntity<Capsule<SearchResult>> findGroupVenues(
      @Valid @RequestBody final GroupSearchRequest request) {
    return ResponseEntity.ok(
        new Capsule<>("Group search completed.", service.findGroupVenues(request)));
  }

  /**
   * Runs proposal Operation 4 and gets one venue by its ID or name.
   *
   * @param request the venue ID or name
   * @return the venue details in a capsule
   * @author Andrew Hsu
   */
  @PostMapping("/info")
  @AuthorizationRequired
  public ResponseEntity<Capsule<VenueInfo>> getVenueInfo(
      @RequestBody final VenueInfoRequest request) {
    return ResponseEntity.ok(new Capsule<>("Venue info found.", infoService.getVenueInfo(request)));
  }
}
