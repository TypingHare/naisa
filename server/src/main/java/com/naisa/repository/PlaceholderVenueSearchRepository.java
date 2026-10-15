package com.naisa.repository;

import com.naisa.model.data.VenueSearchData;
import org.springframework.stereotype.Repository;

/**
 * Reserves the database integration point for combined Operations 1 and 2 and Operation 3.
 *
 * @author Andrew Hsu
 */
@Repository
public class PlaceholderVenueSearchRepository implements VenueSearchRepository {
  /**
   * Reports that the database adapter has not been implemented.
   *
   * <p>TODO: Look up sports.name without regard to case. Set implemented to true and sportFound
   * according to that lookup. Join venue_sports to venues on venue_id and to sports on sport_id.
   * Read opening_intervals for each venue and change day names to ISO day numbers. Map venues.id,
   * name, latitude, longitude, and timezone into each candidate. Map venue_sports.price and its
   * stored facility fields. The schema does not store a price unit; resolve this before setting
   * priceUnit or marking the adapter ready. Group opening periods under each venue to avoid
   * duplicate venues and queries per venue.
   *
   * <p>Keep the interface and data models when replacing this method with the database queries. No
   * invented venue records are returned by this placeholder.
   *
   * @param sport the sport to look up after the database is implemented
   * @return a result that identifies the unimplemented data source
   * @author Andrew Hsu
   */
  @Override
  public VenueSearchData findBySportName(final String sport) {
    return new VenueSearchData();
  }
}
