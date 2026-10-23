package com.naisa.service;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.ReportCrowdResponse;
import com.naisa.repository.CrowdReportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles crowd reports submitted by clients.
 *
 * <p>We allow users to report the crowd level of a venue or a specific sport. If the same user
 * submits another report within two hours, we update the existing report instead of creating
 * a new one.
 *
 * @author Emily Wang
 */
@Service
public class CrowdReportService {
  private final CrowdReportRepository crowdReportRepository;
  private final VenueRepository venueRepository;
  private final VenueSportRepository venueSportRepository;

  /**
   * Creates a crowd report service.
   *
   * @param crowdReportRepository repository for crowd reports
   * @param venueRepository repository for venues
   * @param venueSportRepository repository for venue sports
   * @author Emily Wang
   */
  public CrowdReportService(
      final CrowdReportRepository crowdReportRepository,
      final VenueRepository venueRepository,
      final VenueSportRepository venueSportRepository) {
    this.crowdReportRepository = crowdReportRepository;
    this.venueRepository = venueRepository;
    this.venueSportRepository = venueSportRepository;
  }

  /**
   * Saves a crowd report for a venue.
   *
   * <p>We first check whether the venue exists and offers the requested sport. If we find a report
   * from the same client and user within the past two hours, we update its crowd level and
   * timestamp. Otherwise, we create a new report.
   *
   * <p>We lock the venue row so concurrent submissions cannot create duplicate reports.
   *
   * @param client the client submitting the report
   * @param venueId the venue ID
   * @param request the crowd report request
   * @return the saved report
   * @throws VenueException if the venue does not exist or does not offer the sport
   * @author Emily Wang
   */
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ReportCrowdResponse reportCrowd(
      final Client client, final Long venueId, final ReportCrowdRequest request) {
    final Venue venue =
        venueRepository.findByIdForUpdate(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

    final Sport sport;
    if (request.getSportId() == null) {
      sport = null;
    } else {
      final VenueSport offering =
          venueSportRepository
              .findByVenueIdAndSportId(venueId, request.getSportId())
              .orElseThrow(VenueException.SPORT_NOT_OFFERED::clone);
      sport = offering.getSport();
    }

    final Instant now = Instant.now();
    final Instant cutoff = now.minus(2, ChronoUnit.HOURS);

    final Optional<CrowdReport> existingReport =
        crowdReportRepository.findRecentReport(
            client.getId(),
            request.getExternalUserId(),
            venueId,
            request.getSportId(),
            cutoff);

    if (existingReport.isPresent()) {
      final Long reportId = existingReport.get().getId();
      crowdReportRepository.updateReport(reportId, request.getCrowdLevel(), now);
      return new ReportCrowdResponse(reportId, request.getCrowdLevel(), now, true);
    }

    final CrowdReport report = new CrowdReport();
    report.setClient(client);
    report.setExternalUserId(request.getExternalUserId());
    report.setVenue(venue);
    report.setSport(sport);
    report.setCrowdLevel(request.getCrowdLevel());

    final CrowdReport savedReport = crowdReportRepository.saveAndFlush(report);

    return new ReportCrowdResponse(
        savedReport.getId(), savedReport.getCrowdLevel(), savedReport.getCreatedAt(), false);
  }
}
