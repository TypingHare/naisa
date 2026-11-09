package com.naisa.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.naisa.model.entity.CrowdReport;

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
     * <p>
     * A reporter is identified by the client and the external user ID. Eligible reports include
     * venue-wide reports and reports for the specified sport. If {@code sportId} is null, only
     * venue-wide reports are eligible. Each reporter contributes only their most recent eligible
     * report. Reports with the same creation time are ordered by ID, highest first.
     *
     * @param venueId the ID of the venue
     * @param sportId the ID of the sport, or {@code null} for venue-wide reports only
     * @param cutoff  the earliest creation time to include
     * @return the most recent report from each reporter, newest first
     * @author Emily Wang, Claude Code, James Chen, Codex
     */
    @Query(value = """
        SELECT *
        FROM (
          SELECT DISTINCT ON (client_id, external_user_id) *
          FROM crowd_reports
          WHERE venue_id = :venueId
            AND created_at >= :cutoff
            AND (sport_id IS NULL OR sport_id = CAST(:sportId AS BIGINT))
          ORDER BY client_id, external_user_id, created_at DESC, id DESC
        ) latest_reports
        ORDER BY created_at DESC, id DESC
        """, nativeQuery = true)
    List<CrowdReport> findLatestReports(
        @Param("venueId") Long venueId,
        @Param("sportId") Long sportId,
        @Param("cutoff") Instant cutoff
    );
}
