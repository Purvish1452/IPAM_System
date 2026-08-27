package com.motadata.traceorg.ipam.repository.ipRequests;

import com.motadata.traceorg.ipam.entity.ipRequests.TraceOrgIpRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgIpRequestsRepository extends JpaRepository<TraceOrgIpRequests, Long> {
    List<TraceOrgIpRequests> findByCreatedBy(String createdBy);
}
