package com.naisa.service;

import com.naisa.common.VenueConstant;
import com.naisa.exception.VenueException;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.model.vo.ReportCrowdResponse;
import com.naisa.repository.CrowdReportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles crowd reports submitted by clients.
 *
 * <p>Users can report the crowd level of a venue or a specific sport. Every submission is stored as
 * a new report so that historical data is kept. Get Venue Status counts only each reporter's most
 * recent report when estimating crowdedness.
 *
 * @author Emily Wang, Codex
 */
@Service
public class CrowdReportService {
  private static final String SOURCE_LIVE = "LIVE";
  private static final String SOURCE_UNKNOWN = "UNKNOWN";

  private final CrowdReportRepository crowdReportRepository;
  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;
  private final VenueService venueService;

  /**
   * Creates a crowd report service.
   *
   * @param crowdReportRepository repository for crowd reports
   * @param venueRepository repository for venues
   * @param venueSportRepository repository for venue sports
   * @author Emily Wang, Codex
   */
  public CrowdReportService(
      final CrowdReportRepository crowdReportRepository,
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository,
      final VenueService venueService) {
    this.crowdReportRepository = crowdReportRepository;
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
    this.venueService = venueService;
  }

  /**
   * Saves a new crowd report for a venue.
   *
   * <p>We first check whether the venue exists and offers the requested sport. Then we store the
   * report with the current server time.
   *
   * @param client the client submitting the report
   * @param venueId the venue ID
   * @param request the crowd report request
   * @return the saved report
   * @throws VenueException if the venue does not exist or does not offer the sport
   * @author Emily Wang, Codex, James Chen
   */
  @Transactional
  public ReportCrowdResponse reportCrowd(
      final Client client, final Long venueId, final ReportCrowdRequest request) {
    final Venue venue =
        venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

    final Long sportId = request.getSportId();
    final Sport sport = sportId == null ? null : venueService.getVenueSport(venueId, sportId);

    final CrowdReport report =
        Beans.create(
            CrowdReport.class,
            (it) -> {
              it.setClient(client);
              it.setExternalUserId(request.getExternalUserId());
              it.setVenue(venue);
              it.setSport(sport);
              it.setCrowdLevel(request.getCrowdLevel());
            });
    final CrowdReport savedReport = crowdReportRepository.saveAndFlush(report);

    return Beans.create(
        ReportCrowdResponse.class,
        (it) -> {
          it.setReportId(savedReport.getId());
          it.setCrowdLevel(savedReport.getCrowdLevel());
          it.setCreatedAt(savedReport.getCreatedAt());
        });
  }

  /**
   * Returns the current crowd status of a venue or one of its sports.
   *
   * <p>We use each reporter's most recent report within the cutoff window. If a sport is given but
   * has no recent reports, we fall back to all recent reports for the venue. The crowd level is the
   * most frequently reported level; ties are broken by the most recent report.
   *
   * @param venueId the venue ID
   * @param sportId the sport ID, or {@code null} for the entire venue
   * @return the crowd status
   * @throws VenueException if the venue does not exist or does not offer the sport
   * @author Emily Wang, Claude Code
   */
  @Transactional(readOnly = true)
  public CrowdStatusResponse getCrowdStatus(final Long venueId, final Long sportId) {
    if (!venueRepository.existsById(venueId)) {
      throw VenueException.NOT_FOUND.clone();
    }

    if (sportId != null
        && venueSportRepository.findByVenueIdAndSportId(venueId, sportId).isEmpty()) {
      throw VenueException.SPORT_NOT_OFFERED.clone();
    }

    final Instant cutoff =
        Instant.now().minus(VenueConstant.CROWD_REPORT_CUTOFF_HOURS, ChronoUnit.HOURS);

    final List<CrowdReport> sportReports =
        sportId == null
            ? List.of()
            : crowdReportRepository.findLatestReportsPerReporter(venueId, sportId, cutoff);
    final List<CrowdReport> reports =
        sportReports.isEmpty()
            ? crowdReportRepository.findLatestReportsPerReporter(venueId, null, cutoff)
            : sportReports;

    final CrowdStatusResponse response = new CrowdStatusResponse();
    response.setVenueId(venueId);
    response.setReportCount(reports.size());

    if (reports.isEmpty()) {
      response.setSource(SOURCE_UNKNOWN);
      return response;
    }

    response.setCrowdLevel(findMostFrequentLevel(reports));
    response.setLatestReportAt(reports.get(0).getCreatedAt());
    response.setSource(SOURCE_LIVE);
    return response;
  }

  /**
   * Finds the most frequently reported crowd level.
   *
   * <p>The reports must be sorted from newest to oldest, so that the first report with a tied level
   * is the most recent one.
   */
  private String findMostFrequentLevel(final List<CrowdReport> reports) {
    final Map<String, Integer> counts = new HashMap<>();
    for (final CrowdReport report : reports) {
      counts.merge(report.getCrowdLevel(), 1, Integer::sum);
    }

    final int maxCount = counts.values().stream().max(Integer::compare).orElse(0);
    for (final CrowdReport report : reports) {
      if (counts.get(report.getCrowdLevel()) == maxCount) {
        return report.getCrowdLevel();
      }
    }

    return reports.get(0).getCrowdLevel();
  }
}
