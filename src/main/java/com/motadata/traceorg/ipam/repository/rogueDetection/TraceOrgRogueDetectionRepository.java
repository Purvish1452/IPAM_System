package com.motadata.traceorg.ipam.repository.rogueDetection;

import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgRogueDetectionRepository extends JpaRepository<TraceOrgRogueDetection, Long>
{
    TraceOrgRogueDetection findByMacAddressAndIpAddress(String macAddress, String ipAddress);

    List<TraceOrgRogueDetection> findByAuthenticity(String authenticity);

    List<TraceOrgRogueDetection> findByMacAddress(String macAddress);

    List<TraceOrgRogueDetection> findTop20ByAuthenticityOrderByDiscoveredAtDesc(String authenticity);
}
