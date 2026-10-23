package com.naisa.repository;

import com.naisa.model.entity.CrowdReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides persistence access to crowd reports.
 *
 * @author Emily Wang
 */
@Repository
public interface CrowdReportRepository extends JpaRepository<CrowdReport, Long> {}
