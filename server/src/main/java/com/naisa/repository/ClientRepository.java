package com.naisa.repository;

import com.naisa.model.entity.Client;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents a client repository.
 *
 * @author James Chen
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    /**
     * Inserts a client and returns its ID and API key in one database statement.
     *
     * @param apiKey the API key to use, or {@code null} to generate one in PostgreSQL
     * @return the inserted client, including its API key
     * @author James Chen, Codex
     */
    @Transactional
    @Query(value = """
        INSERT INTO clients (api_key)
        VALUES (COALESCE(CAST(:apiKey AS text), gen_random_uuid()::text))
        RETURNING id, api_key
        """, nativeQuery = true)
    Client insertClient(@Nullable @Param("apiKey") String apiKey);

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
