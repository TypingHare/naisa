package com.naisa.repository;

import com.naisa.model.entity.Review;
import jakarta.annotation.Nullable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to ratings and comments submitted by external users.
 *
 * <p>
 * User identifiers are interpreted together with their client identifiers. A review may concern the
 * entire venue or a particular sport offered there.
 *
 * @author Vincent Lai, Codex
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
	/**
	 * Finds a user's existing review for the requested venue and optional sport.
	 *
	 * <p>
	 * The sport comparison treats two null values as equal, so an omitted sport selects the user's
	 * whole-venue review. A non-null sport selects only the review for that sport.
	 *
	 * @param clientId       identifier of the client that owns the external user namespace
	 * @param externalUserId user identifier supplied by that client
	 * @param venueId        identifier of the reviewed venue
	 * @param sportId        identifier of the reviewed sport, or null for the entire venue
	 * @return the matching review, or an empty result if the user has not reviewed this scope
	 * @author Vincent Lai, Codex
	 */
	@Query(value = """
		SELECT *
		FROM reviews
		WHERE client_id = :clientId
		  AND external_user_id = :externalUserId
		  AND venue_id = :venueId
		  AND sport_id IS NOT DISTINCT FROM CAST(:sportId AS BIGINT)
		""", nativeQuery = true)
	Optional<Review> findExistingReview(
		@Param("clientId") Long clientId,
		@Param("externalUserId") String externalUserId,
		@Param("venueId") Long venueId,
		@Param("sportId") @Nullable Long sportId
	);
}