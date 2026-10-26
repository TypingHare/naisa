package com.naisa.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naisa.model.dto.AddVenueRequest;
import com.naisa.model.dto.FindAlternativesRequest;
import com.naisa.model.dto.FindVenuesRequest;
import com.naisa.model.dto.GroupMemberLocation;
import com.naisa.model.dto.RateVenueRequest;
import com.naisa.model.dto.VenueComparisonRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.Review;
import com.naisa.model.entity.Venue;
import com.naisa.service.ClientService;
import com.naisa.service.VenueService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Verifies numeric request binding, validation, and persistence using the application's JSON mapper.
 *
 * @author Vincent Lai, Codex
 */
@SpringBootTest
class NumericRequestTest {
  @Autowired private JsonMapper jsonMapper;
  @Autowired private Validator validator;
  @Autowired private VenueService venueService;
  @Autowired private ClientService clientService;
  @Autowired private EntityManager entityManager;

  /**
   * Checks coordinate bounds and nonfinite values on every request bean that carries coordinates.
   *
   * @author Vincent Lai, Codex
   */
  @Test
  void validatesDoubleCoordinates() {
    final List<Object> requests = List.of(new AddVenueRequest(), new FindVenuesRequest(),
        new FindAlternativesRequest(), new GroupMemberLocation(), new VenueComparisonRequest());
    for (final Object request : requests) {
      final BeanWrapper wrapper = new BeanWrapperImpl(request);
      for (final String property : List.of("latitude", "longitude")) {
        final double limit = "latitude".equals(property) ? 90.0 : 180.0;
        for (final double valid : new double[] {-limit, 0.125, limit}) {
          wrapper.setPropertyValue(property, valid);
          assertTrue(validator.validateProperty(request, property).isEmpty());
        }
        for (final double invalid : new double[] {
            -limit - 0.1, limit + 0.1, Double.NaN,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
          wrapper.setPropertyValue(property, invalid);
          assertFalse(validator.validateProperty(request, property).isEmpty());
        }
      }
    }
  }

  /**
   * Ensures both supported rating boundaries bind to Short and pass validation.
   *
   * @param rating a supported whole-number rating
   * @author Vincent Lai, Codex
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 5})
  void acceptsWholeNumberRatings(final int rating) {
    final RateVenueRequest request = jsonMapper.readValue(
        "{\"rating\":" + rating + "}", RateVenueRequest.class);
    assertEquals(Short.valueOf((short) rating), request.getRating());
    assertTrue(validator.validateProperty(request, "rating").isEmpty());
  }

  /**
   * Rejects decimal tokens and values outside SMALLINT before they can be rounded or wrapped.
   *
   * @param rating JSON number that cannot be accepted as an integral rating
   * @author Vincent Lai, Codex
   */
  @ParameterizedTest
  @ValueSource(strings = {"4.5", "4.0", "32768", "-32769", "\"4.5\""})
  void rejectsInvalidRatingNumbers(final String rating) {
    assertThrows(JacksonException.class, () -> jsonMapper.readValue(
        "{\"rating\":" + rating + "}", RateVenueRequest.class));
  }

  /**
   * Requires ratings and rejects integers outside the one-to-five range after binding.
   *
   * @param rating JSON value that violates rating validation
   * @author Vincent Lai, Codex
   */
  @ParameterizedTest
  @ValueSource(strings = {"null", "0", "6"})
  void rejectsMissingOrOutOfRangeRatings(final String rating) {
    final RateVenueRequest request = jsonMapper.readValue(
        "{\"rating\":" + rating + "}", RateVenueRequest.class);
    assertFalse(validator.validateProperty(request, "rating").isEmpty());
  }

  /**
   * Saves JSON coordinates and a Short rating through the services and verifies the database values.
   *
   * @author Vincent Lai, Codex
   */
  @Test
  @Transactional
  void persistsCoordinatesAndRating() {
    final Client client = clientService.createClient();
    final AddVenueRequest venueRequest = jsonMapper.readValue("""
        {"name":"Numeric test venue","address":"123 Test Street",
         "latitude":40.812345,"longitude":-73.961234,"timezone":"America/New_York"}
        """, AddVenueRequest.class);
    final Long venueId = venueService.addVenue(client, venueRequest).getId();
    final RateVenueRequest reviewRequest = jsonMapper.readValue("""
        {"externalUserID":"numeric-test-user","rating":5,"comment":"Great venue"}
        """, RateVenueRequest.class);
    final Long reviewId = venueService.rateVenue(client, venueId, reviewRequest).getId();
    entityManager.flush();
    entityManager.clear();

    final Venue venue = entityManager.find(Venue.class, venueId);
    assertEquals(venueRequest.getLatitude(), venue.getLatitude());
    assertEquals(venueRequest.getLongitude(), venue.getLongitude());
    assertEquals(Short.valueOf((short) 5), entityManager.find(Review.class, reviewId).getRating());
  }
}
