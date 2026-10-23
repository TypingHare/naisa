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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles crowd reports submitted by clients.
 *
 * <p>Users can report the crowd level of a venue or a specific sport. Every submission is stored
 * as a new report so that historical data is kept. Get Venue Status counts only each reporter's
 * most recent report when estimating crowdedness.
 *
 * @author Emily Wang, Codex
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
   * @author Emily Wang, Codex
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
   * @author Emily Wang, Codex
   */
  @Transactional
  public ReportCrowdResponse reportCrowd(
      final Client client, final Long venueId, final ReportCrowdRequest request) {
    final Venue venue =
        venueRepository.findById(venueId).orElseThrow(VenueException.NOT_FOUND::clone);

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

    final CrowdReport report = new CrowdReport();
    report.setClient(client);
    report.setExternalUserId(request.getExternalUserId());
    report.setVenue(venue);
    report.setSport(sport);
    report.setCrowdLevel(request.getCrowdLevel());

    final CrowdReport savedReport = crowdReportRepository.saveAndFlush(report);

    final ReportCrowdResponse response = new ReportCrowdResponse();
    response.setReportId(savedReport.getId());
    response.setCrowdLevel(savedReport.getCrowdLevel());
    response.setCreatedAt(savedReport.getCreatedAt());
    return response;
  }
}
