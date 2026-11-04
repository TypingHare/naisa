package com.naisa.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naisa.constant.CrowdConstant;
import com.naisa.exception.CrowdException;
import com.naisa.exception.VenueException;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.repository.CrowdReportRepository;
import com.naisa.util.Beans;

/**
 * Handles crowd reports submitted by clients.
 *
 * <p>
 * Users can report the crowd level of a venue or a specific sport. Every submission is stored as a
 * new report so that historical data is kept. Crowd status uses only each reporter's most recent
 * report when estimating crowdedness.
 *
 * @author Emily Wang, Codex
 */
@Service
public class CrowdReportService {
    /**
     * Maps crowd-level scores from 0 (empty) to 4 (packed) to their crowd-level strings.
     */
    public static final Map<Integer, String> CROWD_LEVEL_STRINGS_BY_SCORE = Map.of(
        0,
        CrowdConstant.CROWD_LEVEL_EMPTY,
        1,
        CrowdConstant.CROWD_LEVEL_LIGHT,
        2,
        CrowdConstant.CROWD_LEVEL_MODERATE,
        3,
        CrowdConstant.CROWD_LEVEL_BUSY,
        4,
        CrowdConstant.CROWD_LEVEL_PACKED
    );

    /**
     * Maps crowd-level strings to their crowd-level scores.
     */
    public static final Map<String, Integer> CROWD_LEVEL_SCORES_BY_STRINGS = Map.of(
        CrowdConstant.CROWD_LEVEL_EMPTY,
        0,
        CrowdConstant.CROWD_LEVEL_LIGHT,
        1,
        CrowdConstant.CROWD_LEVEL_MODERATE,
        2,
        CrowdConstant.CROWD_LEVEL_BUSY,
        3,
        CrowdConstant.CROWD_LEVEL_PACKED,
        4
    );

    /**
     * Crowd-level strings ordered from empty to packed; each level's index is its score (0–4).
     */
    public static final List<String> CROWD_LEVELS = List.of(
        CrowdConstant.CROWD_LEVEL_EMPTY,
        CrowdConstant.CROWD_LEVEL_LIGHT,
        CrowdConstant.CROWD_LEVEL_MODERATE,
        CrowdConstant.CROWD_LEVEL_BUSY,
        CrowdConstant.CROWD_LEVEL_PACKED
    );

    private final CrowdReportRepository crowdReportRepository;
    private final VenueService venueService;

    /**
     * Creates a crowd report service.
     *
     * @param crowdReportRepository repository for crowd reports
     * @param venueService          service for retrieving venues and their sports
     * @author Emily Wang, Codex
     */
    public CrowdReportService(
        final CrowdReportRepository crowdReportRepository,
        final VenueService venueService
    ) {
        this.crowdReportRepository = crowdReportRepository;
        this.venueService = venueService;
    }

    /**
     * Adds a new crowd report for a venue.
     *
     * @param client  the client submitting the report
     * @param venueId the venue ID
     * @param request the crowd report request
     * @return the saved report
     * @throws VenueException if the venue does not exist or does not offer the sport
     * @author Emily Wang, Codex, James Chen
     */
    @Transactional
    public CrowdReport reportCrowd(
        final Client client,
        final Long venueId,
        final ReportCrowdRequest request
    ) {
        final Venue venue = venueService.getVenue(venueId);
        final Long sportId = request.getSportId();
        final Sport sport = sportId == null ? null
            : venueService.getVenueSport(venueId, sportId).getSport();

        final CrowdReport report = Beans.create(CrowdReport.class, (it) -> {
            it.setClient(client);
            it.setExternalUserId(request.getExternalUserId());
            it.setVenue(venue);
            it.setSport(sport);
            it.setCrowdLevel(request.getCrowdLevel());
        });
        return crowdReportRepository.saveAndFlush(report);
    }

    /**
     * Returns the current crowd status of a venue or one of its sports.
     *
     * <p>
     * Uses each reporter's latest eligible report within the cutoff window. Venue-wide reports are
     * always eligible; reports for the requested sport are also eligible when one is specified. The
     * crowd level is obtained by rounding the weighted score to the nearest level. Without eligible
     * reports, the source is {@code UNKNOWN} and the score, level, and latest report time are left
     * unset.
     *
     * @param venueId the venue ID
     * @param sportId the sport ID, or {@code null} for the entire venue
     * @return the crowd status
     * @throws VenueException if the venue does not exist or does not offer the sport
     * @author Emily Wang, Claude Code, James Chen
     */
    @Transactional(readOnly = true)
    public CrowdStatusResponse getCrowdStatus(final Long venueId, final Long sportId) {
        venueService.getVenue(venueId);
        if (sportId != null) {
            venueService.getVenueSport(venueId, sportId);
        }

        // Select each reporter's latest report from venue-wide reports and reports for the
        // specified sport, if any.
        final Instant cutoff = Instant.now().minus(
            CrowdConstant.CROWD_REPORT_CUTOFF_MINUTES,
            ChronoUnit.MINUTES
        );
        final List<CrowdReport> reports = crowdReportRepository.findLatestReports(
            venueId,
            sportId,
            cutoff
        );

        final String source = reports.isEmpty() ? CrowdConstant.SOURCE_UNKNOWN
            : CrowdConstant.SOURCE_LIVE;
        final List<CrowdReport> orderedReports = reports.stream().sorted(
            Comparator.comparing(CrowdReport::getCreatedAt).reversed()
        ).toList();

        return Beans.create(CrowdStatusResponse.class, (it) -> {
            it.setVenueId(venueId);
            it.setSource(source);
            it.setReportCount(reports.size());
            if (source == CrowdConstant.SOURCE_UNKNOWN) {
                return;
            }

            it.setLatestReportAt(orderedReports.getFirst().getCreatedAt());

            final double crowdLevelScore = predictCrowdLevelScore(orderedReports);
            it.setCrowdLevelScore(crowdLevelScore);
            it.setCrowdLevel(CROWD_LEVEL_STRINGS_BY_SCORE.get((int) Math.round(crowdLevelScore)));
        });
    }

    /**
     * Predicts the current crowd-level score for the venue based on a list of past crowd reports.
     *
     * <p>
     * Calculates a weighted average of scores from 0 (empty) to 4 (packed). Each report's weight
     * decays exponentially with its age in whole minutes, halving every half-cutoff period
     * (currently 60 minutes). Venue-wide reports receive half the weight of sport-specific reports
     * of the same age. The weighted sum is divided by the total weight.
     *
     * <p>
     * For example, equally recent venue-wide and sport-specific reports with scores of 0 and 3
     * produce a score of {@code (0.5 * 0 + 1.0 * 3) / (0.5 + 1.0) = 2.0}.
     *
     * <p>
     * This method should not be called with an empty {@code crowdReports} list. If the list is
     * empty, {@code 0.0} is returned.
     *
     * @param crowdReports a list of crowd reports
     * @return the weighted crowd-level score from 0.0 to 4.0, or 0.0 for an empty list
     * @throws CrowdException if a report's crowd-level string or the calculated score is invalid
     * @author James Chen
     */
    private static double predictCrowdLevelScore(final List<CrowdReport> crowdReports) {
        if (crowdReports.isEmpty()) {
            return 0.0;
        }

        double weightedScore = 0.0;
        double totalWeight = 0.0;
        final Instant now = Instant.now();
        final double halfLifeInMinutes = Math.floor(CrowdConstant.CROWD_REPORT_CUTOFF_MINUTES / 2);
        for (final CrowdReport report : crowdReports) {
            final long minutes = Duration.between(report.getCreatedAt(), now).toMinutes();
            final double weight = Math.pow(2, -minutes / halfLifeInMinutes);
            final double correctionFactor = report.getSport() == null ? 0.5 : 1.0;
            final double adjustedWeight = weight * correctionFactor;
            totalWeight += adjustedWeight;
            weightedScore += adjustedWeight * getCrowdLevelScore(report.getCrowdLevel());
        }
        weightedScore /= totalWeight;

        if (weightedScore < 0.0 || weightedScore > 4.0) {
            // Normally, this should not happen if the algorithm above is correct.
            throw CrowdException.INVALID_CROWD_LEVEL_SCORE.clone();
        }

        return weightedScore;
    }

    /**
     * Gets the score corresponding to a crowd-level string.
     *
     * @param crowdLevelString the crowd-level string
     * @return the score from 0 (empty) to 4 (packed)
     * @throws CrowdException if the crowd-level string is invalid
     * @author James Chen
     */
    public static int getCrowdLevelScore(final String crowdLevelString) {
        final Integer score = CROWD_LEVEL_SCORES_BY_STRINGS.getOrDefault(crowdLevelString, null);
        if (score == null) {
            throw CrowdException.INVALID_CROWD_LEVEL_STRING.clone();
        }

        return score;
    }

    /**
     * Finds the most frequently reported crowd level.
     *
     * <p>
     * The reports must be sorted from newest to oldest, so that the first report with a tied level
     * is the most recent one.
     *
     * @deprecated Crowd status now uses a weighted score instead of the most frequent level.
     */
    @SuppressWarnings("unused")
    @Deprecated
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
