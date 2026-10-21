package com.naisa.controller.v1;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.exception.VenueException;
import com.naisa.interceptor.AuthorizationInterceptor;
import com.naisa.model.dto.AddVenueRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.vo.ResourceIdResponse;
import com.naisa.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes venue-related HTTP endpoints.
 *
 * <p>Submitted data is attributed to the client established by the authorization interceptor.
 * Request objects define accepted input, while response objects expose only information intended
 * for API consumers. Business rules and persistence are delegated to the service layer.
 *
 * @author Vincent Lai, Codex
 */
@RestController
@RequestMapping("/v1/venues")
public class VenueController {
  private final VenueService venueService;

  /**
   * Creates the controller with its venue service.
   *
   * @param venueService service responsible for venue creation and reviews
   * @author Vincent Lai, Codex
   */
  public VenueController(final VenueService venueService) {
    this.venueService = venueService;
  }

  /**
   * Accepts basic venue information and creates a venue attributed to its submitter.
   *
   * <p>The client is taken from the authenticated request context. The JSON body supplies the
   * external user identifier and venue information. Field validation occurs before this method is
   * invoked; business-rule failures are handled by the shared exception handler.
   *
   * @param client authenticated client attached by the authorization interceptor
   * @param request validated venue submission
   * @return HTTP 201 with the created venue's identifier inside a capsule
   * @throws VenueException if the submitted timezone is not recognized
   * @author Vincent Lai, Codex
   */
  @AuthorizationRequired
  @PostMapping
  public ResponseEntity<Capsule<ResourceIdResponse>> addVenue(
      @RequestAttribute(AuthorizationInterceptor.AUTHENTICATED_CLIENT_ATTRIBUTE)
          final Client client,
      @Valid @RequestBody final AddVenueRequest request) {
    final ResourceIdResponse result = venueService.addVenue(client, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new Capsule<>("Venue created.", result));
  }

  /**
   * Accepts a rating and comment for a venue or an optional sport offered there.
   *
   * <p>Omitting the sport selects the entire venue. Repeating a submission for the same client,
   * external user, venue, and sport updates the existing review. Both newly created and updated
   * reviews return their identifier with HTTP 200.
   *
   * @param client authenticated client attached by the authorization interceptor
   * @param venueId identifier taken from the request path
   * @param request validated review submission
   * @return HTTP 200 with the saved review's identifier inside a capsule
   * @throws VenueException if the venue is absent or the requested sport is not offered there
   * @author Vincent Lai, Codex
   */
  @AuthorizationRequired
  @PostMapping("/{venueId}/reviews")
  public ResponseEntity<Capsule<ResourceIdResponse>> rateVenue(
      @RequestAttribute(AuthorizationInterceptor.AUTHENTICATED_CLIENT_ATTRIBUTE)
          final Client client,
      @PathVariable("venueId") final Long venueId,
      @Valid @RequestBody final RateVenueRequest request) {
    final ResourceIdResponse result =
        venueService.rateVenue(client, venueId, request);

    return ResponseEntity.ok(new Capsule<>("Review saved.", result));
  }
}