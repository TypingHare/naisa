package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.model.dto.CompareVenuesRequest;
import com.naisa.model.vo.CompareVenuesResponse;
import com.naisa.service.CompareVenuesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles requests for comparing venues.
 *
 * @author Emily Wang, Codex
 */
@RestController
@RequestMapping("/v1/venues")
public class CompareVenuesController {

  private final CompareVenuesService compareVenuesService;

  /**
   * Creates the controller with the venue comparison service.
   *
   * @param compareVenuesService service for comparing venues
   * @author Emily Wang, Codex
   */
  public CompareVenuesController(final CompareVenuesService compareVenuesService) {
    this.compareVenuesService = compareVenuesService;
  }

  /**
   * Compares venues for a requested sport and user location.
   *
   * @param request comparison parameters
   * @return HTTP 200 with the comparison results
   * @author Emily Wang, Codex
   */
  @AuthorizationRequired
  @GetMapping("/comparison")
  public ResponseEntity<Capsule<CompareVenuesResponse>> compareVenues(
      @Valid @ModelAttribute final CompareVenuesRequest request) {
    final CompareVenuesResponse result = compareVenuesService.compareVenues(request);
    return ResponseEntity.ok(new Capsule<>("Venues compared.", result));
  }
}
