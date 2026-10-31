package com.naisa.repository;

import com.naisa.model.entity.Client;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Represents a client repository. */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
	/**
	 * Retrieves a client by an API key.
	 *
	 * @param apiKey the API key of the client
	 * @return a client that is associated with the API key, or {@code null} if no matching client
	 *         exists
	 * @author James Chen
	 */
	@Nullable
	Client findClientByApiKey(String apiKey);
}
