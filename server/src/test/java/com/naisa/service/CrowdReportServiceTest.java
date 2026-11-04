package com.naisa.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.repository.CrowdReportRepository;
import com.naisa.util.Beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CrowdReportService}.
 *
 * <p>
 * Repositories and {@link VenueService} are replaced with Mockito mocks, so no database is needed.
 * Tests are grouped by the service method they exercise.
 *
 * @author Emily Wang, Claude Code
 */
@ExtendWith(MockitoExtension.class)
class CrowdReportServiceTest {
    private static final Long VENUE_ID = 1L;
    private static final Long SPORT_ID = 2L;

    @Mock
    private CrowdReportRepository crowdReportRepository;
    @Mock
    private VenueService venueService;

    @InjectMocks
    private CrowdReportService crowdReportService;

    /** Tests for {@link CrowdReportService#reportCrowd}. */
    @Nested
    @DisplayName("reportCrowd")
    class ReportCrowd {
        private Client client;
        private Venue venue;
        private ReportCrowdRequest request;

        @BeforeEach
        void setUp() {
            client = new Client();
            client.setId(10L);
            venue = new Venue();
            venue.setId(VENUE_ID);

            request = new ReportCrowdRequest();
            request.setExternalUserId("user-1");
            request.setCrowdLevel("BUSY");
        }

        /**
         * Makes the mocked repository return the saved report with an ID and a creation time.
         */
        private void mockSave() {
            when(crowdReportRepository.saveAndFlush(any(CrowdReport.class))).thenAnswer(
                invocation -> {
                    final CrowdReport saved = invocation.getArgument(0);
                    saved.setId(100L);
                    saved.setCreatedAt(Instant.parse("2026-10-24T12:00:00Z"));
                    return saved;
                }
            );
        }

        @Test
        @DisplayName("saves a venue-wide report when no sport is given")
        void savesVenueWideReport() {
            when(venueService.getVenue(VENUE_ID)).thenReturn(venue);
            mockSave();

            final CrowdReport savedReport = crowdReportService.reportCrowd(
                client,
                VENUE_ID,
                request
            );

            final ArgumentCaptor<CrowdReport> captor = ArgumentCaptor.forClass(CrowdReport.class);
            verify(crowdReportRepository).saveAndFlush(captor.capture());
            final CrowdReport saved = captor.getValue();
            assertSame(client, saved.getClient());
            assertSame(venue, saved.getVenue());
            assertNull(saved.getSport());
            assertEquals("user-1", saved.getExternalUserId());
            assertEquals("BUSY", saved.getCrowdLevel());
            verify(venueService, never()).getVenueSport(any(), any());

            assertEquals(100L, savedReport.getId());
            assertEquals("BUSY", savedReport.getCrowdLevel());
            assertEquals(Instant.parse("2026-10-24T12:00:00Z"), savedReport.getCreatedAt());
        }

        @Test
        @DisplayName("attaches the sport when the venue offers it")
        void savesSportReport() {
            final Sport sport = new Sport();
            sport.setId(SPORT_ID);
            request.setSportId(SPORT_ID);
            when(venueService.getVenue(VENUE_ID)).thenReturn(venue);
            final VenueSport venueSport = Beans.create(VenueSport.class, it -> {
                it.setSport(sport);
            });
            when(venueService.getVenueSport(VENUE_ID, SPORT_ID)).thenReturn(venueSport);
            mockSave();

            crowdReportService.reportCrowd(client, VENUE_ID, request);

            final ArgumentCaptor<CrowdReport> captor = ArgumentCaptor.forClass(CrowdReport.class);
            verify(crowdReportRepository).saveAndFlush(captor.capture());
            assertSame(sport, captor.getValue().getSport());
        }

        @Test
        @DisplayName("throws venue.not_found when the venue does not exist")
        void rejectsUnknownVenue() {
            when(venueService.getVenue(VENUE_ID)).thenThrow(VenueException.NOT_FOUND.clone());

            final VenueException exception = assertThrows(
                VenueException.class,
                () -> crowdReportService.reportCrowd(client, VENUE_ID, request)
            );

            assertEquals("venue.not_found", exception.getCode());
            verify(crowdReportRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("throws venue.sport_not_offered when the venue does not offer the sport")
        void rejectsSportNotOffered() {
            request.setSportId(SPORT_ID);
            when(venueService.getVenue(VENUE_ID)).thenReturn(venue);
            when(venueService.getVenueSport(VENUE_ID, SPORT_ID)).thenThrow(
                VenueException.SPORT_NOT_OFFERED.clone()
            );

            final VenueException exception = assertThrows(
                VenueException.class,
                () -> crowdReportService.reportCrowd(client, VENUE_ID, request)
            );

            assertEquals("venue.sport_not_offered", exception.getCode());
            verify(crowdReportRepository, never()).saveAndFlush(any());
        }
    }

    /** Tests for {@link CrowdReportService#getCrowdStatus}. */
    @Nested
    @DisplayName("getCrowdStatus")
    class GetCrowdStatus {

        @Test
        @DisplayName("returns UNKNOWN when there are no recent reports")
        void returnsUnknownWithoutReports() {
            when(crowdReportRepository.findLatestReports(eq(VENUE_ID), isNull(), any())).thenReturn(
                List.of()
            );

            final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID, null);

            assertEquals(VENUE_ID, status.getVenueId());
            assertEquals("UNKNOWN", status.getSource());
            assertEquals(0, status.getReportCount());
            assertNull(status.getCrowdLevel());
            assertNull(status.getLatestReportAt());
        }

        @Test
        @DisplayName("throws venue.not_found when the venue does not exist")
        void rejectsUnknownVenue() {
            when(venueService.getVenue(VENUE_ID)).thenThrow(VenueException.NOT_FOUND.clone());

            final VenueException exception = assertThrows(
                VenueException.class,
                () -> crowdReportService.getCrowdStatus(VENUE_ID, null)
            );

            assertEquals("venue.not_found", exception.getCode());
        }

        @Test
        @DisplayName("throws venue.sport_not_offered when the venue does not offer the sport")
        void rejectsSportNotOffered() {
            when(venueService.getVenueSport(VENUE_ID, SPORT_ID)).thenThrow(
                VenueException.SPORT_NOT_OFFERED.clone()
            );

            final VenueException exception = assertThrows(
                VenueException.class,
                () -> crowdReportService.getCrowdStatus(VENUE_ID, SPORT_ID)
            );

            assertEquals("venue.sport_not_offered", exception.getCode());
        }
    }
}
