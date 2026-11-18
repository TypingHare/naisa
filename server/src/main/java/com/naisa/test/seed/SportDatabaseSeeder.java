package com.naisa.test.seed;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.naisa.model.entity.Sport;
import com.naisa.repository.SportRepository;
import com.naisa.util.Beans;

import net.datafaker.Faker;

/**
 * Represents a sport database seeder.
 *
 * @author Vincent Lai, Codex
 */
@Component
@Priority(300)
public final class SportDatabaseSeeder extends DatabaseSeeder<Sport> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SportDatabaseSeeder.class);
    private static final List<String> SPORT_NAMES = List.of(
        "Basketball",
        "Tennis",
        "Badminton",
        "Table Tennis",
        "Volleyball",
        "Soccer",
        "Swimming",
        "Squash",
        "Pickleball",
        "Baseball"
    );

    private final SportRepository sportRepository;

    public SportDatabaseSeeder(final SportRepository sportRepository) {
        super();
        this.sportRepository = sportRepository;
    }

    /**
     * Generates the requested number of sports using supported sport names.
     *
     * @param seed  the seed to use for random data generation
     * @param count the number of sports to generate
     * @return the generated sports
     * @author Vincent Lai, Codex
     */
    @Override
    public List<Sport> generateEntities(final long seed, final int count) {
        final Faker faker = getFaker(seed);
        final List<Sport> sports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sports.add(Beans.create(Sport.class, it -> {
                it.setName(faker.options().nextElement(SPORT_NAMES));
            }));
        }

        return sports;
    }

    /**
     * Inserts the generated sports into the database and logs the insertion.
     *
     * @param entities the sports to insert
     * @author Vincent Lai, Codex
     */
    @Override
    public void seed(final List<Sport> entities) {
        sportRepository.saveAll(entities);
        LOGGER.info("Inserted {} seed sports into the database.", entities.size());
    }
}
