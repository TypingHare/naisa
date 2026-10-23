package com.naisa.repository;

import com.naisa.model.entity.CrowdReport;
import java.time.Instant;
import java.util.List;
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
   * Finds each reporter's most recent report for a venue since the cutoff time.
   *
   * <p>A reporter is identified by the client and the external user ID. If {@code sportId} is
   * null, reports for all sports at the venue are included. The result is sorted from newest to
   * oldest.
   *
   * @param venueId the ID of the venue
   * @param sportId the ID of the sport, or {@code null} to include all reports for the venue
   * @param cutoff the earliest creation time to include
   * @return the most recent report from each reporter, newest first
   * @author Emily Wang, Claude Code
   */
  @Query(
      value = """
          SELECT *
          FROM (
            SELECT DISTINCT ON (client_id, external_user_id) *
            FROM crowd_reports
            WHERE venue_id = :venueId
              AND created_at >= :cutoff
              AND (CAST(:sportId AS BIGINT) IS NULL OR sport_id = CAST(:sportId AS BIGINT))
            ORDER BY client_id, external_user_id, created_at DESC, id DESC
          ) latest_reports
          ORDER BY created_at DESC, id DESC
          """,
      nativeQuery = true)
  List<CrowdReport> findLatestReportsPerReporter(
      @Param("venueId") Long venueId,
      @Param("sportId") Long sportId,
      @Param("cutoff") Instant cutoff);
}
