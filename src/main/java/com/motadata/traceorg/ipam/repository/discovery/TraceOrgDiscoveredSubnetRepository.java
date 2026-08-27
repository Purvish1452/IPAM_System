package com.motadata.traceorg.ipam.repository.discovery;

import com.motadata.traceorg.ipam.entity.discovery.TraceOrgDiscoveredSubnet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgDiscoveredSubnetRepository extends JpaRepository<TraceOrgDiscoveredSubnet, Integer>
{
     List<TraceOrgDiscoveredSubnet> findBySubnetAndSubnetMask(String subnet, String subnetMask);
}
