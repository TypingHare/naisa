package com.naisa.repository;

import com.naisa.model.entity.OpeningInterval;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides access to opening hours for each venue.
 *
 * <p>A venue may have multiple opening intervals on the same day.
 *
 * @author Emily Wang
 */
@Repository
public interface OpeningIntervalRepository extends JpaRepository<OpeningInterval, Long> {

  List<OpeningInterval> findByVenueId(Long venueId);
}
