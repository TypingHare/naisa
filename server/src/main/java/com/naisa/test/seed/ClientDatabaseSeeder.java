package com.naisa.test.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.naisa.model.entity.Client;
import com.naisa.repository.ClientRepository;
import com.naisa.util.Beans;

import jakarta.annotation.Priority;

/**
 * Represents a client database seeder.
 *
 * @author James Chen
 */
@Component
@Priority(100)
public final class ClientDatabaseSeeder extends DatabaseSeeder {
    private final ClientRepository clientRepository;

    public ClientDatabaseSeeder(final ClientRepository clientRepository) {
        super();
        this.clientRepository = clientRepository;
    }

    /**
     * Inserts 100 clients with generated API keys into the database.
     *
     * @author James Chen
     */
    @Override
    public void seed() {
        final List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            clients.add(Beans.create(Client.class));
        }
        clientRepository.saveAll(clients);
    }
}
