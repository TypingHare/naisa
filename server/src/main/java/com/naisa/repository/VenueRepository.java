package com.naisa.repository;

import com.naisa.model.entity.Venue;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to venue records.
 *
 * <p>
 * The locking lookup supports transactions that must coordinate changes associated with the same
 * venue, including concurrent review submissions.
 *
 * @author Vincent Lai, Codex
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
	/**
	 * Finds a venue and acquires a write lock on its database row.
	 *
	 * <p>
	 * The caller must execute this query inside a transaction. The lock is retained until that
	 * transaction completes, allowing a subsequent review lookup and write to run without another
	 * participating submission changing the same venue's reviews concurrently.
	 *
	 * @param venueId identifier of the venue to retrieve
	 * @return the matching venue, or an empty result if it does not exist
	 * @author Vincent Lai, Codex
	 */
	@Query(value = """
		SELECT *
		FROM venues
		WHERE id = :venueId
		FOR UPDATE
		""", nativeQuery = true)
	Optional<Venue> findByIdForUpdate(@Param("venueId") Long venueId);
}