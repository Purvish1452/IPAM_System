package com.motadata.traceorg.ipam.repository.discovery;

import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgGatewayRepository extends JpaRepository<TraceOrgGateway, Long>
{
    boolean existsByGateway(String gateway);

    List<TraceOrgGateway> findByIdNotAndGateway(Long id, String gateway);

    List<TraceOrgGateway> findById(Long gatewayId);
}
