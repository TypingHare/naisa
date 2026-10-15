package com.naisa.repository;

import com.naisa.model.entity.OpeningInterval;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Reads weekly hours for one venue.
 *
 * @author Andrew Hsu
 */
@Repository
public interface VenueInfoHoursRepository extends JpaRepository<OpeningInterval, Long> {
  /**
   * Reads all weekly opening periods for one venue.
   *
   * @author Andrew Hsu
   */
  List<OpeningInterval> findByVenueId(Long venueId);
}
