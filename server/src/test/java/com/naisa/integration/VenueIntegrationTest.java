package com.naisa.integration;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.naisa.common.Capsule;
import com.naisa.constant.VenueConstant;
import com.naisa.model.vo.VenueSearchResultResponse;
import com.naisa.util.MvcResponse;
import com.naisa.util.Route;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests venue API endpoints.
 *
 * @author James Chen, Codex
 */
@Order(1000)
class VenueIntegrationTest extends IntegrationTest {
    /**
     * Verifies that a venue search returns an empty list when no venues lie within the radius.
     *
     * @author James Chen, Codex
     */
    @Test
    void searchReturnsEmptyListWhenNoVenuesAreNearby() throws Exception {
        final LocalDate tomorrow = LocalDate.now(ZoneId.of(VenueConstant.REQUEST_TIMEZONE))
            .plusDays(1);
        // The test database seeds venues in New York City.
        final String path = "/v1/venues?coordinates[0].latitude=0&coordinates[0].longitude=0"
            + "&sportId=1&time=12:00&maxDistance=1&budget=100&date=" + tomorrow;
        final MvcResponse<VenueSearchResultResponse[]> response = request(
            new Route(HttpMethod.GET, path),
            VenueSearchResultResponse[].class
        );

        assertEquals(HttpStatus.OK.value(), response.mvcResult().getResponse().getStatus());
        final Capsule<VenueSearchResultResponse[]> capsule = response.capsule();
        assertNull(capsule.getErrorCode());
        assertEquals("No venues match the search parameters.", capsule.getMessage());
        assertArrayEquals(new VenueSearchResultResponse[0], capsule.getData());
    }
}
