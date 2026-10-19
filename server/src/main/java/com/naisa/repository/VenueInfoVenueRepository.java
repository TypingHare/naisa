package com.naisa.repository;

import com.naisa.model.entity.Venue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Reads venues for the info operation.
 *
 * @author Andrew Hsu
 */
@Repository
public interface VenueInfoVenueRepository extends JpaRepository<Venue, Long> {
  /**
   * Finds all venues with the given name, without regard to letter case.
   *
   * @param name the full venue name
   * @return all matching venues, including multiple rows when the name is ambiguous
   * @author Andrew Hsu
   */
  List<Venue> findByNameIgnoreCase(String name);
}
