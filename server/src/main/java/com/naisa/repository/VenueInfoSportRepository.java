package com.naisa.repository;

import com.naisa.model.entity.VenueSport;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Reads all sports at one venue.
 *
 * @author Andrew Hsu
 */
@Repository
public interface VenueInfoSportRepository extends JpaRepository<VenueSport, Long> {
  /**
   * Reads sports with their names in one query.
   *
   * @param venueId the stored venue ID
   * @return the venue's sports, or an empty list when none are stored
   * @author Andrew Hsu
   */
  @EntityGraph(attributePaths = "sport")
  List<VenueSport> findByVenueId(Long venueId);
}
