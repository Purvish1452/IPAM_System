package com.motadata.traceorg.ipam.repository.alert;

import com.motadata.traceorg.ipam.entity.alert.TraceOrgAlertStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgAlertStreamRepository extends JpaRepository<TraceOrgAlertStream, Long>
{
    List<TraceOrgAlertStream> findBySubnetIdAndAlertTypeAndStatus(Long subnetId, String alertType, Boolean status);

    Page<TraceOrgAlertStream> findByStatusOrderByTimestampDesc(Boolean status, Pageable pageable);

    int countByStatus(Boolean status);
}

