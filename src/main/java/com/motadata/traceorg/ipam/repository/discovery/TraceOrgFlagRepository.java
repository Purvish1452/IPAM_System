package com.motadata.traceorg.ipam.repository.discovery;

import com.motadata.traceorg.ipam.entity.discovery.TraceOrgFlags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgFlagRepository extends JpaRepository<TraceOrgFlags, String>
{
    TraceOrgFlags findByFlag(String flagName);
}
