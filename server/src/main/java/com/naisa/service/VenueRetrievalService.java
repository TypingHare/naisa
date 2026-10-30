package com.naisa.service;

import com.naisa.model.entity.Venue;
import com.naisa.model.vo.OpeningIntervalVo;
import com.naisa.model.vo.SportVo;
import com.naisa.model.vo.VenueVo;
import com.naisa.util.Beans;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles advanced venue retrieval.
 *
 * @author James Chen
 */
@Service
public class VenueRetrievalService {
  private final VenueService venueService;

  /**
   * Creates a {@code VenueRetrievalService} instance.
   *
   * @param venueService service for basic venue operations
   * @author James Chen
   */
  public VenueRetrievalService(final VenueService venueService) {
    this.venueService = venueService;
  }

  /**
   * Retrieves a venue's location, offered sports, and weekly opening intervals.
   *
   * <p>Sports and opening intervals are returned without sorting. Missing sports or intervals
   * produce empty lists. Hours are returned in the venue's local time, including overnight
   * intervals. Current availability is not checked.
   *
   * @param venueId identifier of the venue
   * @return the venue details with its sports and opening intervals
   * @throws com.naisa.exception.VenueException if the venue does not exist
   * @author Andrew Hsu, Codex, Vincent Lai, Codex, James Chen
   */
  @Transactional(readOnly = true)
  public VenueVo getVenueVo(final Long venueId) {
    final Venue venue = venueService.getVenue(venueId);
    final List<SportVo> sportVos = venueService.getSportVos(venueId);
    final List<OpeningIntervalVo> openIntervalVos = venueService.getOpeningIntervalVos(venueId);
    return Beans.create(
        VenueVo.class,
        (it) -> {
          it.setVenueId(venue.getId());
          it.setName(venue.getName());
          it.setAddress(venue.getAddress());
          it.setLatitude(venue.getLatitude());
          it.setLongitude(venue.getLongitude());
          it.setTimezone(venue.getTimezone());
          it.setSports(sportVos);
          it.setOpeningIntervals(openIntervalVos);
        });
  }
}
