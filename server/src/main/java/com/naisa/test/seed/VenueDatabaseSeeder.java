package com.naisa.test.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.naisa.constant.VenueConstant;
import com.naisa.model.entity.Client;
import com.naisa.model.entity.Venue;
import com.naisa.repository.ClientRepository;
import com.naisa.repository.VenueRepository;
import com.naisa.util.Beans;

import net.datafaker.Faker;

/**
 * Represents a venue database seeder.
 *
 * @author James Chen
 */
@Component
@Priority(200)
public final class VenueDatabaseSeeder extends DatabaseSeeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(VenueDatabaseSeeder.class);
    private static final List<String> VENUE_NAMES = List.of(
        "Sports Center",
        "Recreation Center",
        "Athletic Club",
        "Community Courts",
        "Sports Complex"
    );

    private final ClientRepository clientRepository;
    private final VenueRepository venueRepository;

    public VenueDatabaseSeeder(
        final ClientRepository clientRepository,
        final VenueRepository venueRepository
    ) {
        super();
        this.clientRepository = clientRepository;
        this.venueRepository = venueRepository;
    }

    /**
     * Inserts 100 venues into the database.
     *
     * @param seed the seed for random data generation
     * @author James Chen
     */
    @Override
    public void seed(final long seed) {
        final List<Client> clients = clientRepository.findAll(Sort.by("id"));
        final Faker faker = new Faker(Locale.US, new Random(seed));

        final List<Venue> venues = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final Client client = faker.options().nextElement(clients);
            venues.add(Beans.create(Venue.class, (it) -> {
                final String venueName = faker.options().nextElement(VENUE_NAMES) + " " + faker
                    .number().numberBetween(1000, 9999);

                // The rough boundary of NYC is (40.49, -74.26) to (40.92, -73.70).
                final double latitude = faker.number().numberBetween(40_490_000, 40_920_000)
                    / 1_000_000.0;
                final double longitude = faker.number().numberBetween(-74_260_000, -73_700_000)
                    / 1_000_000.0;

                it.setName(venueName);
                it.setAddress(faker.address().streetAddress());
                it.setLatitude(latitude);
                it.setLongitude(longitude);
                it.setTimezone(VenueConstant.REQUEST_TIMEZONE);
                it.setCreatedByClient(client);
            }));
        }

        venueRepository.saveAll(venues);
        LOGGER.info("Inserted {} seed venues into the database.", venues.size());
    }
}
