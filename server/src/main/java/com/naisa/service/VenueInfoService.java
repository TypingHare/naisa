package com.naisa.service;

import com.naisa.exception.VenueInfoException;
import com.naisa.model.dto.Location;
import com.naisa.model.dto.VenueInfoRequest;
import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.FacilityInfo;
import com.naisa.model.vo.VenueInfo;
import com.naisa.model.vo.VenueOpeningHours;
import com.naisa.model.vo.VenueSportInfo;
import com.naisa.repository.VenueInfoHoursRepository;
import com.naisa.repository.VenueInfoSportRepository;
import com.naisa.repository.VenueInfoVenueRepository;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gets stored details for one venue.
 *
 * @author Andrew Hsu, Codex
 */
@Service
public final class VenueInfoService {
  private final VenueInfoVenueRepository venueRepository;
  private final VenueInfoSportRepository sportRepository;
  private final VenueInfoHoursRepository hoursRepository;

  /**
   * Connects the venue info operation to its data sources.
   *
   * @author Andrew Hsu
   */
  public VenueInfoService(
      final VenueInfoVenueRepository venueRepository,
      final VenueInfoSportRepository sportRepository,
      final VenueInfoHoursRepository hoursRepository) {
    this.venueRepository = venueRepository;
    this.sportRepository = sportRepository;
    this.hoursRepository = hoursRepository;
  }

  /**
   * Gets one venue by ID or exact name.
   *
   * @author Andrew Hsu
   */
  @Transactional(readOnly = true)
  public VenueInfo getVenueInfo(final VenueInfoRequest request) {
    final Venue venue = findVenue(request);
    final VenueInfo result = new VenueInfo();
    result.setVenueId(venue.getId().toString());
    result.setVenueName(venue.getName());
    result.setAddress(venue.getAddress());
    result.setTimezone(venue.getTimezone());
    final Location location = new Location();
    location.setLatitude(venue.getLatitude());
    location.setLongitude(venue.getLongitude());
    result.setLocation(location);
    result.setSports(
        sportRepository.findByVenueId(venue.getId()).stream()
            .sorted(Comparator.comparing(row -> row.getSport().getName()))
            .map(this::sportInfo)
            .toList());
    result.setOpeningHours(
        hoursRepository.findByVenueId(venue.getId()).stream()
            .sorted(
                Comparator.comparingInt(
                        (OpeningInterval row) -> DayOfWeek.valueOf(row.getDayOfWeek()).getValue())
                    .thenComparing(OpeningInterval::getOpensAt)
                    .thenComparing(OpeningInterval::getClosesAt))
            .map(this::openingHours)
            .toList());
    return result;
  }

  /**
   * Checks that one selector is present and finds the venue.
   *
   * @author Andrew Hsu
   */
  private Venue findVenue(final VenueInfoRequest request) {
    final String id = request.getVenueId();
    final String name = request.getVenueName();
    if ((id == null) == (name == null)) {
      throw VenueInfoException.INVALID_SELECTOR.clone();
    }
    if (id != null) {
      if (!id.matches("[1-9][0-9]*")) {
        throw VenueInfoException.INVALID_SELECTOR.clone();
      }
      final long parsedId;
      try {
        parsedId = Long.parseLong(id);
      } catch (NumberFormatException exception) {
        throw VenueInfoException.INVALID_SELECTOR.clone();
      }
      return venueRepository.findById(parsedId)
          .orElseThrow(VenueInfoException.NOT_FOUND::clone);
    }
    final String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      throw VenueInfoException.INVALID_SELECTOR.clone();
    }
    final List<Venue> matches = venueRepository.findByNameIgnoreCase(trimmedName);
    if (matches.isEmpty()) {
      throw VenueInfoException.NOT_FOUND.clone();
    }
    if (matches.size() > 1) {
      throw VenueInfoException.AMBIGUOUS_NAME.clone();
    }
    return matches.getFirst();
  }

  /**
   * Maps one stored sport to a response item.
   *
   * @author Andrew Hsu
   */
  private VenueSportInfo sportInfo(final VenueSport row) {
    final VenueSportInfo info = new VenueSportInfo();
    info.setSport(row.getSport().getName());
    info.setPriceUsd(row.getPrice());
    if (row.getIsIndoor() != null || row.getSurfaceType() != null) {
      final FacilityInfo facility = new FacilityInfo();
      facility.setIsIndoor(row.getIsIndoor());
      facility.setSurfaceType(row.getSurfaceType());
      info.setFacility(facility);
    }
    return info;
  }

  /**
   * Maps one stored opening period to a response item.
   *
   * @author Andrew Hsu
   */
  private VenueOpeningHours openingHours(final OpeningInterval row) {
    final VenueOpeningHours hours = new VenueOpeningHours();
    hours.setDayOfWeek(row.getDayOfWeek());
    hours.setOpensAt(row.getOpensAt());
    hours.setClosesAt(row.getClosesAt());
    return hours;
  }
}
