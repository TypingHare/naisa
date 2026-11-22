package com.naisa.integration;

import java.util.List;

import org.springframework.http.HttpMethod;

import com.naisa.util.Route;

/**
 * Lists API routes used by integration tests.
 *
 * @author James Chen
 */
final class Routes {
    /**
     * Lists every route the authorization test should exercise.
     *
     * <p>
     * Add each new protected endpoint here. Replace path parameters with concrete values so MockMvc
     * can send the request, for example {@code /v1/venues/1}.
     *
     * @author James Chen
     */
    public static final List<Route> AuthorizationRequiredRoutes = List.of(
        new Route(HttpMethod.GET, "/v1/venues"),
        new Route(HttpMethod.GET, "/v1/venues/alternatives"),
        new Route(HttpMethod.GET, "/v1/venues/comparison"),
        new Route(HttpMethod.GET, "/v1/venues/1"),
        new Route(HttpMethod.POST, "/v1/venues"),
        new Route(HttpMethod.POST, "/v1/venues/1/reviews"),
        new Route(HttpMethod.POST, "/v1/venues/1/crowd-reports"),
        new Route(HttpMethod.GET, "/v1/venues/1/crowd-status")
    );
}
