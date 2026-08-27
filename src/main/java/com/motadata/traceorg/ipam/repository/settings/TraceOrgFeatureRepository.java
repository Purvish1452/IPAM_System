package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TraceOrgFeatureRepository extends JpaRepository<TraceOrgFeature, Long> {
    TraceOrgFeature findByName(String featureName);
}
