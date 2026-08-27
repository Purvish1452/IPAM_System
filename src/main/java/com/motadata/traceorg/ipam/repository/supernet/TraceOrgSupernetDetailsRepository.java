package com.motadata.traceorg.ipam.repository.supernet;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgSupernetCategory;
import com.motadata.traceorg.ipam.entity.supernet.TraceOrgSupernetDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgSupernetDetailsRepository extends JpaRepository<TraceOrgSupernetDetails,Long>
{
    TraceOrgSupernetDetails findBySubnetId(String stringValue);

    TraceOrgSupernetDetails findBySubnetIdAndTraceOrgSupernetCategory(String subnetId, TraceOrgSupernetCategory traceOrgSupernetCategory);
}
