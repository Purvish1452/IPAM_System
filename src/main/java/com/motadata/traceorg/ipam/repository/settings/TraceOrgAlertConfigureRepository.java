package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgAlertConfigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgAlertConfigureRepository extends JpaRepository<TraceOrgAlertConfigure, String>
{
    TraceOrgAlertConfigure findByAlertKey(String alertKey);
}
