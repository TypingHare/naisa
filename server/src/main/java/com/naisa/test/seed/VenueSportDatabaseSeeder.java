package com.naisa.test.seed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.naisa.model.entity.Sport;
import com.naisa.model.entity.Venue;
import com.naisa.model.entity.VenueSport;
import com.naisa.repository.SportRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.repository.VenueSportRepository;
import com.naisa.util.Beans;

import net.datafaker.Faker;

/**
 * Represents a venue sport database seeder, run after venues and sports are inserted.
 *
 * @author Vincent Lai, Codex
 */
@Component
@Priority(400)
public final class VenueSportDatabaseSeeder extends DatabaseSeeder<VenueSport> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VenueSportDatabaseSeeder.class);

    private final VenueRepository venueRepository;
    private final SportRepository sportRepository;
    private final VenueSportRepository venueSportRepository;

    public VenueSportDatabaseSeeder(
        final VenueRepository venueRepository,
        final SportRepository sportRepository,
        final VenueSportRepository venueSportRepository
    ) {
        super();
        this.venueRepository = venueRepository;
        this.sportRepository = sportRepository;
        this.venueSportRepository = venueSportRepository;
    }

    /**
     * Generates distinct venue-sport pairs with prices and facility attributes.
     *
     * <p>
     * Each pass visits every venue and advances its sport by one, avoiding duplicate pairs.
     *
     * @param seed  the seed to use for random data generation
     * @param count the number of venue sports to generate
     * @return the generated venue sports
     * @throws IllegalArgumentException if the count exceeds the available distinct pairs
     * @author Vincent Lai, Codex
     */
    @Override
    public List<VenueSport> generateEntities(final long seed, final int count) {
        final List<Venue> venues = venueRepository.findAll(Sort.by("id"));
        final List<Sport> sports = sportRepository.findAll(Sort.by("id"));
        if (count > (long) venues.size() * sports.size()) {
            throw new IllegalArgumentException("Not enough distinct venue-sport pairs to seed.");
        }
        final Faker faker = getFaker(seed);

        final List<VenueSport> venueSports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final int venueIndex = i % venues.size();
            final Venue venue = venues.get(venueIndex);
            final Sport sport = sports.get((venueIndex + i / venues.size()) % sports.size());
            venueSports.add(Beans.create(VenueSport.class, it -> {
                it.setVenue(venue);
                it.setSport(sport);
                it.setPrice(BigDecimal.valueOf(faker.number().numberBetween(0, 10_001), 2));
                it.setIsIndoor(faker.bool().bool());
                it.setHasLighting(faker.bool().bool());
            }));
        }

        return venueSports;
    }

    /**
     * Inserts the generated venue sports into the database and logs the insertion.
     *
     * @param entities the venue sports to insert
     * @author Vincent Lai, Codex
     */
    @Override
    public void seed(final List<VenueSport> entities) {
        venueSportRepository.saveAll(entities);
        LOGGER.info("Inserted {} seed venue sports into the database.", entities.size());
    }
}
