package com.motadata.traceorg.ipam.repository.dashboard;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgCategoryRepository extends JpaRepository<TraceOrgCategory, Long>
{

}
