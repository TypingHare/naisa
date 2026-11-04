package com.naisa.controller.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.naisa.common.Capsule;
import com.naisa.common.annotation.AuthorizationRequired;
import com.naisa.exception.VenueException;
import com.naisa.interceptor.AuthorizationInterceptor;
import com.naisa.model.dto.CompareVenuesRequest;
import com.naisa.model.dto.CreateVenueRequest;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.dto.SearchAlternativeVenuesRequest;
import com.naisa.model.dto.SearchVenuesRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Venue;
import com.naisa.model.vo.AlternativeVenueResponse;
import com.naisa.model.vo.CompareVenuesResponse;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.model.vo.ResourceIdResponse;
import com.naisa.model.vo.VenueResponse;
import com.naisa.model.vo.VenueSearchResultResponse;
import com.naisa.service.CrowdReportService;
import com.naisa.service.VenueComparisonService;
import com.naisa.service.VenueRetrievalService;
import com.naisa.service.VenueSearchService;
import com.naisa.service.VenueService;
import com.naisa.util.Resources;

/**
 * Exposes venue-related HTTP endpoints.
 *
 * <p>
 * Submitted data is attributed to the client established by the authorization interceptor. Request
 * objects define accepted input, while response objects expose only information intended for API
 * consumers. Business rules and persistence are delegated to the service layer.
 *
 * @author Vincent Lai, Codex, Andrew Hsu, Codex
 */
@RestController
@RequestMapping("/v1/venues")
public class VenueController {
    private final VenueService venueService;
    private final VenueSearchService venueSearchService;
    private final VenueRetrievalService venueRetrievalService;
    private final CrowdReportService crowdReportService;
    private final VenueComparisonService venueComparisonService;

    /**
     * Creates the controller with its services.
     *
     * @param venueService           service for basic venue operations, creation, and reviews
     * @param venueSearchService     service for venue searches and alternatives
     * @param venueRetrievalService  service for assembling venue details
     * @param crowdReportService     service responsible for crowd reports
     * @param venueComparisonService service responsible for venue comparison
     * @author Vincent Lai, Codex, Emily Wang, Codex, Claude Code, Andrew Hsu, Codex
     */
    public VenueController(
        final VenueService venueService,
        final VenueSearchService venueSearchService,
        final VenueRetrievalService venueRetrievalService,
        final CrowdReportService crowdReportService,
        final VenueComparisonService venueComparisonService
    ) {
        this.venueService = venueService;
        this.venueSearchService = venueSearchService;
        this.venueRetrievalService = venueRetrievalService;
        this.crowdReportService = crowdReportService;
        this.venueComparisonService = venueComparisonService;
    }

    /**
     * Returns a venue's details, sports, and opening intervals.
     *
     * @param venueId identifier taken from the request path
     * @return HTTP 200 with the venue details inside a capsule
     * @throws VenueException if the venue does not exist
     * @author Andrew Hsu, Codex
     */
    @AuthorizationRequired
    @GetMapping("/{venueId}")
    public ResponseEntity<Capsule<VenueResponse>> getVenueInfo(
        @PathVariable("venueId") final Long venueId
    ) {
        final VenueResponse venueResponse = venueRetrievalService.getVenueResponse(venueId);
        return ResponseEntity.ok(new Capsule<>("Venue retrieved.", venueResponse));
    }

    /**
     * Finds open venues within the requested distance and hourly budget for one origin or a group.
     *
     * <p>
     * Query parameters supply an indexed list of member coordinates, including a single coordinate
     * for an individual search. Results have no guaranteed distance ordering.
     *
     * @param request validated search query parameters
     * @return HTTP 200 with matching venues, or an empty list when none match
     * @throws VenueException if the requested time is in the past or the sport does not exist
     * @author Andrew Hsu, Codex
     */
    @AuthorizationRequired
    @GetMapping
    public ResponseEntity<Capsule<List<VenueSearchResultResponse>>> findVenues(
        @Valid @ModelAttribute final SearchVenuesRequest request
    ) {
        final List<VenueSearchResultResponse> results = venueSearchService.searchVenues(request);
        final String message = results.isEmpty() ? "No venues match the search parameters."
            : "Venues found.";
        return ResponseEntity.ok(new Capsule<>(message, results));
    }

    /**
     * Finds venue alternatives using only the changes permitted by the client.
     *
     * @param request validated search query parameters and permitted changes
     * @return HTTP 200 with alternatives, or an empty list when none match
     * @throws VenueException if the original time is in the past, the sport does not exist, or no
     *                        venue offers the sport within the permitted radius
     * @author Andrew Hsu, Codex
     */
    @AuthorizationRequired
    @GetMapping("/alternatives")
    public ResponseEntity<Capsule<List<AlternativeVenueResponse>>> findAlternatives(
        @Valid @ModelAttribute final SearchAlternativeVenuesRequest request
    ) {
        final List<AlternativeVenueResponse> results = venueSearchService.searchAlternatives(
            request
        );
        final String message = results.isEmpty() ? "No alternatives match the permitted changes."
            : "Alternatives found.";
        return ResponseEntity.ok(new Capsule<>(message, results));
    }

    /**
     * Accepts basic venue information and creates a venue attributed to its submitter.
     *
     * <p>
     * The client is taken from the authenticated request context. The JSON body supplies the venue
     * information. Field validation occurs before this method is invoked; business-rule failures
     * are handled by the shared exception handler.
     *
     * @param client  authenticated client attached by the authorization interceptor
     * @param request validated venue submission
     * @return HTTP 201 with the created venue's identifier inside a capsule
     * @throws VenueException if the submitted timezone is not recognized
     * @author Vincent Lai, Codex
     */
    @AuthorizationRequired
    @PostMapping
    public ResponseEntity<Capsule<ResourceIdResponse>> addVenue(
        @RequestAttribute(AuthorizationInterceptor.REQUEST_ATTRIBUTE_CLIENT) final Client client,
        @Valid @RequestBody final CreateVenueRequest request
    ) {
        final Venue venue = venueService.addVenue(client, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new Capsule<>("Venue created.", Resources.createResponseFrom(venue))
        );
    }

    /**
     * Accepts a rating and comment for a venue or an optional sport offered there.
     *
     * <p>
     * Omitting the sport selects the entire venue. Repeating a submission for the same client,
     * external user, venue, and sport updates the existing review. Both newly created and updated
     * reviews return their identifier with HTTP 200.
     *
     * @param client  authenticated client attached by the authorization interceptor
     * @param venueId identifier taken from the request path
     * @param request validated review submission
     * @return HTTP 200 with the saved review's identifier inside a capsule
     * @throws VenueException if the venue is absent or the requested sport is not offered there
     * @author Vincent Lai, Codex
     */
    @AuthorizationRequired
    @PostMapping("/{venueId}/reviews")
    public ResponseEntity<Capsule<ResourceIdResponse>> rateVenue(
        @RequestAttribute(AuthorizationInterceptor.REQUEST_ATTRIBUTE_CLIENT) final Client client,
        @PathVariable("venueId") final Long venueId,
        @Valid @RequestBody final RateVenueRequest request
    ) {
        final Review review = venueService.rateVenue(client, venueId, request);
        return ResponseEntity.ok(
            new Capsule<>("Review saved.", Resources.createResponseFrom(review))
        );
    }

    /**
     * Accepts a crowd report for a venue or an optional sport.
     *
     * <p>
     * Every submission creates a new report.
     *
     * @param client  authenticated client attached by the authorization interceptor
     * @param venueId identifier of the venue
     * @param request validated crowd report request
     * @return HTTP 201 with the created report's ID
     * @throws VenueException if the venue does not exist or does not offer the sport
     * @author Emily Wang, Codex
     */
    @AuthorizationRequired
    @PostMapping("/{venueId}/crowd-reports")
    public ResponseEntity<Capsule<ResourceIdResponse>> reportCrowd(
        @RequestAttribute(AuthorizationInterceptor.REQUEST_ATTRIBUTE_CLIENT) final Client client,
        @PathVariable("venueId") final Long venueId,
        @Valid @RequestBody final ReportCrowdRequest request
    ) {
        final CrowdReport crowdReport = crowdReportService.reportCrowd(client, venueId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new Capsule<>("Crowd report created.", Resources.createResponseFrom(crowdReport))
        );
    }

    /**
     * Returns the current crowd status of a venue or one of its sports.
     *
     * @param venueId identifier of the venue
     * @param sportId optional identifier of the sport
     * @return HTTP 200 with the crowd status
     * @throws VenueException if the venue does not exist or does not offer the sport
     * @author Emily Wang, Claude Code
     */
    @AuthorizationRequired
    @GetMapping("/{venueId}/crowd-status")
    public ResponseEntity<Capsule<CrowdStatusResponse>> getCrowdStatus(
        @PathVariable("venueId") final Long venueId,
        @RequestParam(value = "sportId", required = false) final Long sportId
    ) {
        final CrowdStatusResponse response = crowdReportService.getCrowdStatus(venueId, sportId);
        return ResponseEntity.ok(new Capsule<>("Crowd status retrieved.", response));
    }

    /**
     * Compares 2 to 5 venues for a sport based on the user's location.
     *
     * <p>
     * Parameters are read from the query string, for example {@code
     * ?venueIds=1,2&sportId=1&latitude=40.81&longitude=-73.96}.
     *
     * @param request validated comparison parameters
     * @return HTTP 200 with the comparison results
     * @throws VenueException if the IDs are duplicated, the sport or a venue does not exist, or a
     *                        venue does not offer the sport
     * @author Emily Wang, Codex
     */
    @AuthorizationRequired
    @GetMapping("/comparison")
    public ResponseEntity<Capsule<CompareVenuesResponse>> getVenueComparison(
        @Valid @ModelAttribute final CompareVenuesRequest request
    ) {
        final CompareVenuesResponse result = venueComparisonService.compare(request);
        return ResponseEntity.ok(new Capsule<>("Venues compared.", result));
    }
}
