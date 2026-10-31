package com.naisa.repository;

import com.naisa.model.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides access to registered sports.
 *
 * <p>
 * The inherited {@code existsById} method lets the service check whether a requested sport exists
 * before searching for venues that offer it.
 *
 * @author Emily Wang, Andrew Hsu, Codex
 */
@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
}
