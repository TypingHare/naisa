package com.naisa.repository;

import com.naisa.model.data.OpeningInterval;
import com.naisa.model.data.VenueCandidate;
import com.naisa.model.data.VenueSearchData;
import com.naisa.model.dto.Location;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.model.vo.FacilityInfo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads stored candidates for combined Operations 1 and 2 and for Operation 3.
 *
 * @author Andrew Hsu, Codex
 */
@Repository
public class PostgresVenueSearchRepository implements VenueSearchRepository {
  private final EntityManager entityManager;

  /**
   * Connects search reads to JPA.
   *
   * @param entityManager reads the stored entities
   * @author Andrew Hsu, Codex
   */
  public PostgresVenueSearchRepository(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Reads a sport and its venue records in at most three queries.
   *
   * <p>Opening periods are loaded in bulk. Missing price metadata stays unknown; the search service
   * decides whether a price can be compared with the requested budget.
   *
   * @param sport the trimmed sport name, matched without regard to letter case
   * @return the sport status and stored candidates
   * @author Andrew Hsu, Codex
   */
  @Override
  @Transactional(readOnly = true)
  public VenueSearchData findBySportName(final String sport) {
    final VenueSearchData result = new VenueSearchData();
    final long sportCount =
        entityManager
            .createQuery(
                "select count(s) from sports s where lower(s.name) = lower(:sport)", Long.class)
            .setParameter("sport", sport)
            .getSingleResult();
    result.setSportFound(sportCount > 0);
    if (sportCount == 0) {
      return result;
    }
    final List<VenueSport> rows =
        entityManager
            .createQuery(
                """
                select vs from VenueSport vs
                join fetch vs.venue join fetch vs.sport s
                where lower(s.name) = lower(:sport)
                order by vs.id
                """,
                VenueSport.class)
            .setParameter("sport", sport)
            .getResultList();
    if (rows.isEmpty()) {
      return result;
    }
    final List<Long> venueIds =
        rows.stream().map(row -> row.getVenue().getId()).distinct().toList();
    final Map<Long, List<OpeningInterval>> hours =
        entityManager
            .createQuery(
                """
                select h from OpeningInterval h join fetch h.venue
                where h.venue.id in :venueIds
                order by h.venue.id, h.dayOfWeek, h.opensAt, h.closesAt
                """,
                com.naisa.model.entity.OpeningInterval.class)
            .setParameter("venueIds", venueIds)
            .getResultList().stream()
            .collect(
                Collectors.groupingBy(
                    row -> row.getVenue().getId(),
                    Collectors.mapping(this::openingPeriod, Collectors.toList())));
    result.setVenues(
        rows.stream()
            .map(row -> candidate(row, hours.getOrDefault(row.getVenue().getId(), List.of())))
            .toList());
    return result;
  }

  /**
   * Copies stored values into the search model without exposing JPA entities.
   *
   * @param row the stored venue sport
   * @param hours the venue's weekly opening periods
   * @return the search candidate
   * @author Andrew Hsu, Codex
   */
  private VenueCandidate candidate(final VenueSport row, final List<OpeningInterval> hours) {
    final Venue venue = row.getVenue();
    final VenueCandidate result = new VenueCandidate();
    result.setVenueId(venue.getId());
    result.setVenueName(venue.getName());
    result.setSport(row.getSport().getName());
    final Location location = new Location();
    location.setLatitude(venue.getLatitude());
    location.setLongitude(venue.getLongitude());
    result.setLocation(location);
    result.setTimezone(venue.getTimezone());
    result.setPrice(row.getPrice());
    result.setCurrency(row.getCurrency());
    result.setPriceUnit(row.getPriceUnit());
    final FacilityInfo facility = new FacilityInfo();
    facility.setIsIndoor(row.getIsIndoor());
    facility.setSurfaceType(row.getSurfaceType());
    result.setFacility(facility);
    result.setOpeningHours(List.copyOf(hours));
    return result;
  }

  /**
   * Changes the stored weekday enum to the search model's ISO day number.
   *
   * @param row the stored opening period
   * @return the search opening period, where Monday is 1 and Sunday is 7
   * @author Andrew Hsu, Codex
   */
  private OpeningInterval openingPeriod(final com.naisa.model.entity.OpeningInterval row) {
    final OpeningInterval result = new OpeningInterval();
    result.setDayOfWeek(row.getDayOfWeek().getValue());
    result.setOpensAt(row.getOpensAt());
    result.setClosesAt(row.getClosesAt());
    return result;
  }
}
