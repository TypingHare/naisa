package com.naisa.repository;

import com.naisa.model.data.VenueSearchData;

/**
 * Supplies database-independent data for venue searches.
 *
 * @author Andrew Hsu
 */
public interface VenueSearchRepository {
  /**
   * Reads a sport and its venues, prices, facility fields, and opening hours.
   *
   * @param sport the trimmed sport name, matched without regard to case
   * @return the adapter status and matching data
   * @author Andrew Hsu
   */
  VenueSearchData findBySportName(String sport);
}
