package com.naisa.integration;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.naisa.model.entity.Client;
import com.naisa.repository.ClientRepository;
import com.naisa.util.Route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verifies API key authorization for protected endpoints.
 *
 * @author James Chen, Codex
 */
@Order(100)
class ApiAuthorizationTest extends IntegrationTest {
    @Autowired
    private ClientRepository clientRepository;

    /**
     * Verifies that each protected route listed in {@link Routes} returns HTTP 401 for a missing
     * authorization header, a truncated API key, or an unknown API key. A valid key must pass the
     * authorization check even if the endpoint rejects the request for another reason.
     *
     * @author James Chen
     */
    @Test
    void certainEndpointsShouldRequireAuthorization() throws Exception {
        final List<Client> clients = clientRepository.findAll();
        final String validApiKey = clients.getLast().getApiKey();
        final Set<String> validApiKeySet = clients.stream().map(Client::getApiKey).collect(
            Collectors.toSet()
        );
        String invalidApiKey = null;
        while (invalidApiKey == null || validApiKeySet.contains(invalidApiKey)) {
            invalidApiKey = UUID.randomUUID().toString();
        }

        for (final Route route : Routes.AuthorizationRequiredRoutes) {
            final HttpMethod method = route.method();
            final String path = route.path();
            MvcResult result;

            // Omit the Authorization header.
            result = mvc.perform(MockMvcRequestBuilders.request(method, path)).andReturn();
            assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                result.getResponse().getStatus(),
                () -> String.format("'%s' should reject a missing Authorization header", route)
            );

            // Send a bearer token shorter than 36 characters.
            result = mvc.perform(
                MockMvcRequestBuilders.request(method, path).header(
                    "Authorization",
                    "Bearer incomplete"
                )
            ).andReturn();
            assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                result.getResponse().getStatus(),
                () -> String.format(
                    "'%s' should reject if the 'Authorization' header is incomplete",
                    route
                )
            );

            // Send a full-length key that belongs to no client.
            result = mvc.perform(
                MockMvcRequestBuilders.request(method, path).header(
                    "Authorization",
                    "Bearer " + invalidApiKey
                )
            ).andReturn();
            assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                result.getResponse().getStatus(),
                () -> String.format("'%s' should reject if the API key is invalid", route)
            );

            // A valid key may still produce a validation error after authorization.
            result = mvc.perform(
                MockMvcRequestBuilders.request(method, path).header(
                    "Authorization",
                    "Bearer " + validApiKey
                )
            ).andReturn();
            assertNotEquals(
                HttpStatus.UNAUTHORIZED.value(),
                result.getResponse().getStatus(),
                () -> String.format("'%s' should accept if the API key is valid.", route)
            );
        }
    }
}
