package com.naisa.repository;

import com.naisa.model.entity.VenueSport;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to the sports offered at individual venues.
 *
 * <p>A venue-sport record establishes that a sport is supported at a location and stores the
 * attributes of that offering.
 *
 * @author Vincent Lai, Andrew Hsu, Codex
 */
@Repository
public interface VenueSportRepository extends JpaRepository<VenueSport, Long> {
  /**
   * Finds all sports offered at a venue, with the associated sport records loaded.
   *
   * <p>An empty list means the venue has no recorded offerings. This query does not check
   * whether the venue itself exists.
   *
   * @param venueId identifier of the venue
   * @return offerings with their sport names, prices, and indoor status
   * @author Andrew Hsu, Codex
   */
  @EntityGraph(attributePaths = "sport")
  List<VenueSport> findByVenueId(Long venueId);

  /**
   * Finds the offering that connects a venue to a sport.
   *
   * <p>An empty result means the requested venue-sport combination is not registered, even if the
   * venue and sport exist independently.
   *
   * @param venueId identifier of the venue
   * @param sportId identifier of the sport
   * @return the matching offering, or an empty result if the combination is absent
   * @author Vincent Lai, Codex
   */
  Optional<VenueSport> findByVenueIdAndSportId(Long venueId, Long sportId);

  /**
   * Finds offerings for the requested sport within the hourly budget.
   *
   * @param sportId identifier of the requested sport
   * @param budget maximum hourly price in dollars, inclusive
   * @return matching offerings with their associated venues and sports
   * @author Andrew Hsu, Codex
   */
  @EntityGraph(attributePaths = {"venue", "sport"})
  List<VenueSport> findBySportIdAndPriceLessThanEqual(Long sportId, BigDecimal budget);

  /**
   * Finds all offerings for a sport before distance, price, or opening-hour checks.
   *
   * @param sportId identifier of the requested sport
   * @return offerings with their associated venues and sports
   * @author Andrew Hsu, Codex
   */
  @EntityGraph(attributePaths = {"venue", "sport"})
  List<VenueSport> findBySportId(Long sportId);
}
