package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgMailServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgMailServerRepository extends JpaRepository<TraceOrgMailServer, Long>
{
    @Query("SELECT MAX(m.id) FROM TraceOrgMailServer m")
    Long findMaxId();

}
