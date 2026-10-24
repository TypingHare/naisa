package com.naisa.repository;

import com.naisa.model.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides access to registered sports.
 *
 * @author Emily Wang
 */
@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {}
