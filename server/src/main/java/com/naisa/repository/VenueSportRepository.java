package com.naisa.repository;

import com.naisa.model.entity.VenueSport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to the sports offered at individual venues.
 *
 * <p>A venue-sport record establishes that a sport is supported at a location and stores the
 * attributes of that offering.
 *
 * @author Vincent Lai, Codex
 */
@Repository
public interface VenueSportRepository extends JpaRepository<VenueSport, Long> {
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
  Optional<VenueSport> findByVenue_IdAndSport_Id(Long venueId, Long sportId);
}