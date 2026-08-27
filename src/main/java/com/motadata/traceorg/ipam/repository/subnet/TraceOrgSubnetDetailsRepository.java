package com.motadata.traceorg.ipam.repository.subnet;


import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgSubnetDetailsRepository extends JpaRepository<TraceOrgSubnetDetails, Long>
{
    List<TraceOrgSubnetDetails> findByGatewayId(Long gatewayId);

    @Query(value = "SELECT * FROM subnet_details WHERE total_ip > 0 ORDER BY (used_ip * 100.0 / total_ip) DESC LIMIT 10", nativeQuery = true)
    List<TraceOrgSubnetDetails> findTop10ByUtilization();
}
