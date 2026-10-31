package com.naisa.repository;

import com.naisa.model.entity.OpeningInterval;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides access to opening hours for each venue.
 *
 * <p>A venue may have multiple opening intervals on the same day.
 *
 * @author Emily Wang, Andrew Hsu, Codex
 */
@Repository
public interface OpeningIntervalRepository extends JpaRepository<OpeningInterval, Long> {
  /**
   * Retrieves all opening intervals for the venue with the given ID.
   *
   * @param venueId the ID of the venue
   * @return opening intervals belonging to the venue
   * @author James Chen
   */
  List<OpeningInterval> findByVenueId(Long venueId);

  /**
   * Retrieves opening intervals for the supplied venue identifiers.
   *
   * @param venueIds identifiers of the candidate venues
   * @return opening intervals belonging to those venues
   * @author Andrew Hsu, Codex
   */
  @EntityGraph(attributePaths = "venue")
  List<OpeningInterval> findByVenueIdIn(Collection<Long> venueIds);
}
