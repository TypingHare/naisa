package com.naisa.test.seed;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.naisa.model.entity.OpeningInterval;
import com.naisa.model.entity.Venue;
import com.naisa.repository.OpeningIntervalRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.util.Beans;

import net.datafaker.Faker;

/**
 * Represents an opening interval database seeder, run after venues are inserted.
 *
 * @author Vincent Lai, Codex
 */
@Component
@Priority(500)
public final class OpeningIntervalDatabaseSeeder extends DatabaseSeeder<OpeningInterval> {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        OpeningIntervalDatabaseSeeder.class
    );

    private final VenueRepository venueRepository;
    private final OpeningIntervalRepository openingIntervalRepository;

    public OpeningIntervalDatabaseSeeder(
        final VenueRepository venueRepository,
        final OpeningIntervalRepository openingIntervalRepository
    ) {
        super();
        this.venueRepository = venueRepository;
        this.openingIntervalRepository = openingIntervalRepository;
    }

    /**
     * Generates daytime opening intervals, visiting each venue before reusing one.
     *
     * @param seed  the seed to use for random data generation
     * @param count the number of opening intervals to generate
     * @return the generated opening intervals
     * @author Vincent Lai, Codex
     */
    @Override
    public List<OpeningInterval> generateEntities(final long seed, final int count) {
        final List<Venue> venues = venueRepository.findAll(Sort.by("id"));
        final Faker faker = getFaker(seed);

        final List<OpeningInterval> openingIntervals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Venue venue = venues.get(i % venues.size());
            openingIntervals.add(Beans.create(OpeningInterval.class, it -> {
                it.setVenue(venue);
                it.setDayOfWeek(faker.options().option(DayOfWeek.class));
                it.setOpensAt(LocalTime.of(faker.number().numberBetween(6, 11), 0));
                it.setClosesAt(LocalTime.of(faker.number().numberBetween(17, 24), 0));
            }));
        }

        return openingIntervals;
    }

    /**
     * Inserts the generated opening intervals into the database and logs the insertion.
     *
     * @param entities the opening intervals to insert
     * @author Vincent Lai, Codex
     */
    @Override
    public void seed(final List<OpeningInterval> entities) {
        openingIntervalRepository.saveAll(entities);
        LOGGER.info("Inserted {} seed opening intervals into the database.", entities.size());
    }
}
