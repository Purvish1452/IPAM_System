package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgDatabaseMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgDatabaseMaintenanceRepository extends JpaRepository<TraceOrgDatabaseMaintenance, Long> { }
