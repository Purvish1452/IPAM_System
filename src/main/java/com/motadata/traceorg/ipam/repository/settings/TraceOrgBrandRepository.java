package com.motadata.traceorg.ipam.repository.settings;


import com.motadata.traceorg.ipam.entity.settings.TraceOrgBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgBrandRepository extends JpaRepository<TraceOrgBrand, Long> {}
