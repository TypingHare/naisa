package com.naisa;

import com.naisa.model.data.OpeningInterval;
import com.naisa.model.data.VenueCandidate;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.VenueSearchRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates invented records only for search tests.
 *
 * @author Andrew Hsu, Codex
 */
public final class VenueSearchTestData {
  private VenueSearchTestData() {}

  public static Location location(final double latitude, final double longitude) {
    final Location location = new Location();
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    return location;
  }

  public static VenueSearchRequest request() {
    final VenueSearchRequest request = new VenueSearchRequest();
    request.setLocation(location(0, 0));
    request.setSport("basketball");
    request.setDate(LocalDate.of(2026, 10, 17));
    request.setTime(LocalTime.of(18, 0));
    request.setMaxDistance(5.0);
    return request;
  }

  public static OpeningInterval hours(final int weekday, final String opens, final String closes) {
    final OpeningInterval hours = new OpeningInterval();
    hours.setDayOfWeek(weekday);
    hours.setOpensAt(LocalTime.parse(opens));
    hours.setClosesAt(LocalTime.parse(closes));
    return hours;
  }

  public static VenueCandidate venue(
      final long identifier, final double longitude, final String price) {
    final VenueCandidate venue = new VenueCandidate();
    venue.setVenueId(identifier);
    venue.setVenueName("Test venue " + identifier);
    venue.setSport("basketball");
    venue.setLocation(location(0, longitude));
    venue.setTimezone("UTC");
    venue.setPrice(new BigDecimal(price));
    venue.setPriceUnit("PER_COURT_HOUR");
    final List<OpeningInterval> intervals = new ArrayList<>();
    for (int weekday = 1; weekday <= 7; weekday++) {
      intervals.add(hours(weekday, "09:00", "22:00"));
    }
    venue.setOpeningHours(intervals);
    return venue;
  }

  public static VenueSearchData data(final VenueCandidate... venues) {
    final VenueSearchData data = new VenueSearchData();
    data.setImplemented(true);
    data.setSportFound(true);
    data.setVenues(List.of(venues));
    return data;
  }
}
