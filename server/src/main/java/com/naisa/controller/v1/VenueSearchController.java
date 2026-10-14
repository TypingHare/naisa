package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.model.dto.AlternativeSearchRequest;
import com.naisa.model.dto.GroupSearchRequest;
import com.naisa.model.dto.VenueSearchRequest;
import com.naisa.model.vo.SearchResult;
import com.naisa.service.VenueSearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the three venue search operations.
 *
 * @author Andrew Hsu
 */
@RestController
@RequestMapping("/v1/venues")
public class VenueSearchController {
  private final VenueSearchService service;

  /**
   * Connects the endpoints to the search service.
   *
   * @author Andrew Hsu
   */
  public VenueSearchController(final VenueSearchService service) {
    this.service = service;
  }

  /**
   * Finds exact matches or bounded alternatives.
   *
   * @author Andrew Hsu
   */
  @PostMapping("/search")
  @AuthorizationRequired
  public ResponseEntity<Capsule<SearchResult>> findVenues(
      @Valid @RequestBody final VenueSearchRequest request) {
    return ResponseEntity.ok(new Capsule<>("Venue search completed.", service.findVenues(request)));
  }

  /**
   * Finds alternatives while preserving fixed criteria.
   *
   * @author Andrew Hsu
   */
  @PostMapping("/alternatives")
  @AuthorizationRequired
  public ResponseEntity<Capsule<SearchResult>> findAlternatives(
      @Valid @RequestBody final AlternativeSearchRequest request) {
    return ResponseEntity.ok(
        new Capsule<>("Alternative search completed.", service.findAlternatives(request)));
  }

  /**
   * Finds venues for a group and returns each member's distance.
   *
   * @author Andrew Hsu
   */
  @PostMapping("/group-search")
  @AuthorizationRequired
  public ResponseEntity<Capsule<SearchResult>> findGroupVenues(
      @Valid @RequestBody final GroupSearchRequest request) {
    return ResponseEntity.ok(
        new Capsule<>("Group search completed.", service.findGroupVenues(request)));
  }
}
