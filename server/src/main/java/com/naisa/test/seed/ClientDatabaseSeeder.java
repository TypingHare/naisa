package com.naisa.test.seed;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.naisa.model.entity.Client;
import com.naisa.repository.ClientRepository;
import com.naisa.util.Beans;

import net.datafaker.Faker;

/**
 * Represents a client database seeder.
 *
 * @author James Chen
 */
@Component
@Priority(100)
public final class ClientDatabaseSeeder extends DatabaseSeeder<Client> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDatabaseSeeder.class);

    private final ClientRepository clientRepository;

    public ClientDatabaseSeeder(final ClientRepository clientRepository) {
        super();
        this.clientRepository = clientRepository;
    }

    /**
     * Generates the requested number of clients with API keys.
     *
     * @param seed  the seed to use for random data generation
     * @param count the number of clients to generate
     * @return the generated clients
     * @author James Chen
     */
    @Override
    public List<Client> generateEntities(final long seed, final int count) {
        final Faker faker = getFaker(seed);
        final List<Client> clients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            clients.add(Beans.create(Client.class, it -> {
                it.setApiKey(faker.regexify("[a-zA-Z0-9]{36}"));
            }));
        }

        return clients;
    }

    /**
     * Inserts the generated clients into the database and logs the insertion.
     *
     * @param entities the clients to insert
     * @author James Chen
     */
    @Override
    public void seed(final List<Client> entities) {
        clientRepository.saveAll(entities);
        LOGGER.info("Inserted {} seed clients into the database.", entities.size());
    }
}
