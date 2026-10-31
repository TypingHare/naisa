package com.naisa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.naisa.exception.VenueException;
import com.naisa.model.dto.ReportCrowdRequest;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.CrowdReport;
import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.CrowdStatusResponse;
import com.naisa.model.vo.ReportCrowdResponse;
import com.naisa.repository.CrowdReportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	private VenueRepository venueRepository;
	@Mock
	private VenueSportRepository venueSportRepository;
	@Mock
	private VenueService venueService;

	@InjectMocks
	private CrowdReportService crowdReportService;

	/** Creates a crowd report with the given level, creation time, and ID. */
	private static CrowdReport report(final Long id, final String level, final Instant createdAt) {
		final CrowdReport report = new CrowdReport();
		report.setId(id);
		report.setCrowdLevel(level);
		report.setCreatedAt(createdAt);
		return report;
	}

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
			when(crowdReportRepository.saveAndFlush(any(CrowdReport.class)))
				.thenAnswer(invocation -> {
					final CrowdReport saved = invocation.getArgument(0);
					saved.setId(100L);
					saved.setCreatedAt(Instant.parse("2026-10-24T12:00:00Z"));
					return saved;
				});
		}

		@Test
		@DisplayName("saves a venue-wide report when no sport is given")
		void savesVenueWideReport() {
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
			mockSave();

			final ReportCrowdResponse response = crowdReportService.reportCrowd(client, VENUE_ID,
				request);

			final ArgumentCaptor<CrowdReport> captor = ArgumentCaptor.forClass(CrowdReport.class);
			verify(crowdReportRepository).saveAndFlush(captor.capture());
			final CrowdReport saved = captor.getValue();
			assertSame(client, saved.getClient());
			assertSame(venue, saved.getVenue());
			assertNull(saved.getSport());
			assertEquals("user-1", saved.getExternalUserId());
			assertEquals("BUSY", saved.getCrowdLevel());
			verifyNoInteractions(venueService);

			assertEquals(100L, response.getReportId());
			assertEquals("BUSY", response.getCrowdLevel());
			assertEquals(Instant.parse("2026-10-24T12:00:00Z"), response.getCreatedAt());
		}

		@Test
		@DisplayName("attaches the sport when the venue offers it")
		void savesSportReport() {
			final Sport sport = new Sport();
			sport.setId(SPORT_ID);
			request.setSportId(SPORT_ID);
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
			when(venueService.getVenueSport(VENUE_ID, SPORT_ID)).thenReturn(sport);
			mockSave();

			crowdReportService.reportCrowd(client, VENUE_ID, request);

			final ArgumentCaptor<CrowdReport> captor = ArgumentCaptor.forClass(CrowdReport.class);
			verify(crowdReportRepository).saveAndFlush(captor.capture());
			assertSame(sport, captor.getValue().getSport());
		}

		@Test
		@DisplayName("throws venue.not_found when the venue does not exist")
		void rejectsUnknownVenue() {
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.empty());

			final VenueException exception = assertThrows(VenueException.class,
				() -> crowdReportService.reportCrowd(client, VENUE_ID, request));

			assertEquals("venue.not_found", exception.getCode());
			verify(crowdReportRepository, never()).saveAndFlush(any());
		}

		@Test
		@DisplayName("throws venue.sport_not_offered when the venue does not offer the sport")
		void rejectsSportNotOffered() {
			request.setSportId(SPORT_ID);
			when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
			when(venueService.getVenueSport(VENUE_ID, SPORT_ID))
				.thenThrow(VenueException.SPORT_NOT_OFFERED.clone());

			final VenueException exception = assertThrows(VenueException.class,
				() -> crowdReportService.reportCrowd(client, VENUE_ID, request));

			assertEquals("venue.sport_not_offered", exception.getCode());
			verify(crowdReportRepository, never()).saveAndFlush(any());
		}
	}

	/** Tests for {@link CrowdReportService#getCrowdStatus}. */
	@Nested
	@DisplayName("getCrowdStatus")
	class GetCrowdStatus {
		private final Instant newest = Instant.parse("2026-10-24T12:30:00Z");
		private final Instant middle = Instant.parse("2026-10-24T12:00:00Z");
		private final Instant oldest = Instant.parse("2026-10-24T11:30:00Z");

		@Test
		@DisplayName("returns UNKNOWN when there are no recent reports")
		void returnsUnknownWithoutReports() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), isNull(), any()))
				.thenReturn(List.of());

			final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID, null);

			assertEquals(VENUE_ID, status.getVenueId());
			assertEquals("UNKNOWN", status.getSource());
			assertEquals(0, status.getReportCount());
			assertNull(status.getCrowdLevel());
			assertNull(status.getLatestReportAt());
		}

		@Test
		@DisplayName("returns the most frequently reported level")
		void returnsMostFrequentLevel() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), isNull(), any()))
				.thenReturn(List.of(report(3L, "LIGHT", newest), report(2L, "BUSY", middle),
					report(1L, "BUSY", oldest)));

			final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID, null);

			assertEquals("BUSY", status.getCrowdLevel());
			assertEquals(3, status.getReportCount());
			assertEquals(newest, status.getLatestReportAt());
			assertEquals("LIVE", status.getSource());
		}

		@Test
		@DisplayName("breaks a tie with the most recent report")
		void breaksTieWithMostRecentReport() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), isNull(), any()))
				.thenReturn(List.of(report(2L, "PACKED", newest), report(1L, "EMPTY", oldest)));

			final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID, null);

			assertEquals("PACKED", status.getCrowdLevel());
		}

		@Test
		@DisplayName("uses only the sport's reports when the sport has recent reports")
		void usesSportReports() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(venueSportRepository.findByVenueIdAndSportId(VENUE_ID, SPORT_ID))
				.thenReturn(Optional.of(new VenueSport()));
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), eq(SPORT_ID),
				any())).thenReturn(List.of(report(1L, "EMPTY", newest)));

			final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID,
				SPORT_ID);

			assertEquals("EMPTY", status.getCrowdLevel());
			verify(crowdReportRepository, never()).findLatestReportsPerReporter(eq(VENUE_ID),
				isNull(), any());
		}

		@Test
		@DisplayName("falls back to venue-wide reports when the sport has none")
		void fallsBackToVenueReports() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(venueSportRepository.findByVenueIdAndSportId(VENUE_ID, SPORT_ID))
				.thenReturn(Optional.of(new VenueSport()));
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), eq(SPORT_ID),
				any())).thenReturn(List.of());
			when(crowdReportRepository.findLatestReportsPerReporter(eq(VENUE_ID), isNull(), any()))
				.thenReturn(List.of(report(1L, "MODERATE", newest)));

			final CrowdStatusResponse status = crowdReportService.getCrowdStatus(VENUE_ID,
				SPORT_ID);

			assertEquals("MODERATE", status.getCrowdLevel());
			assertEquals("LIVE", status.getSource());
		}

		@Test
		@DisplayName("throws venue.not_found when the venue does not exist")
		void rejectsUnknownVenue() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(false);

			final VenueException exception = assertThrows(VenueException.class,
				() -> crowdReportService.getCrowdStatus(VENUE_ID, null));

			assertEquals("venue.not_found", exception.getCode());
		}

		@Test
		@DisplayName("throws venue.sport_not_offered when the venue does not offer the sport")
		void rejectsSportNotOffered() {
			when(venueRepository.existsById(VENUE_ID)).thenReturn(true);
			when(venueSportRepository.findByVenueIdAndSportId(VENUE_ID, SPORT_ID))
				.thenReturn(Optional.empty());

			final VenueException exception = assertThrows(VenueException.class,
				() -> crowdReportService.getCrowdStatus(VENUE_ID, SPORT_ID));

			assertEquals("venue.sport_not_offered", exception.getCode());
		}
	}
}
