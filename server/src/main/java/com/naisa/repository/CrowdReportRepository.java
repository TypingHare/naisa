package com.naisa.repository;

import com.naisa.model.entity.CrowdReport;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to crowd reports.
 *
 * @author Emily Wang
 */
@Repository
public interface CrowdReportRepository extends JpaRepository<CrowdReport, Long> {
  /**
   * Finds the most recent report from the same user for a venue and optional sport.
   *
   * @param clientId the ID of the authenticated client
   * @param externalUserId the end user ID provided by the client
   * @param venueId the ID of the venue
   * @param sportId the ID of the sport, or {@code null} for a venue-wide report
   * @param cutoff the earliest creation time to include
   * @return the most recent matching report, or an empty optional if none exists
   * @author Emily Wang, Codex
   */
  @Query(
      value = """
          SELECT *
          FROM crowd_reports
          WHERE client_id = :clientId
            AND external_user_id = :externalUserId
            AND venue_id = :venueId
            AND sport_id IS NOT DISTINCT FROM CAST(:sportId AS BIGINT)
            AND created_at >= :cutoff
          ORDER BY created_at DESC, id DESC
          LIMIT 1
          """,
      nativeQuery = true)
  Optional<CrowdReport> findRecentReport(
      @Param("clientId") Long clientId,
      @Param("externalUserId") String externalUserId,
      @Param("venueId") Long venueId,
      @Param("sportId") Long sportId,
      @Param("cutoff") Instant cutoff);
}
