package com.naisa.service;

import com.naisa.exception.ClientException;
import com.naisa.model.entity.Client;
import com.naisa.repository.ClientRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

/**
 * Represents a client service.
 *
 * @author James Chen
 */
@Service
public final class ClientService {
  private final ClientRepository clientRepository;

  /**
   * Creates a {@code ClientService} instance.
   *
   * @param clientRepository repository for client records
   * @author James Chen
   */
  private ClientService(final ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  /**
   * Retrieves a client by an API key.
   *
   * <p>In this project, a valid API key is defined as an API key that exists in the database. This
   * method only checks whether the key was issued by this service.
   *
   * @param apiKey the API key to look up
   * @return the retrieved client
   * @throws ClientException if the provided API key is invalid
   * @author James Chen
   */
  @NotNull
  public Client getClientByApiKey(final String apiKey) {
    final Client client = this.clientRepository.findClientByApiKey(apiKey);
    if (client == null) {
      throw ClientException.INVALID_API_KEY.clone();
    }

    return client;
  }

  /**
   * Creates a client.
   *
   * <p>The created {@code Client} instance contains a unique API key.
   *
   * @return the created client
   * @author James Chen
   */
  @NotNull
  public Client createClient() {
    return this.clientRepository.save(new Client());
  }
}
