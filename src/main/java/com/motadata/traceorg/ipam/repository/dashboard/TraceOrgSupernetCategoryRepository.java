package com.motadata.traceorg.ipam.repository.dashboard;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgSupernetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceOrgSupernetCategoryRepository extends JpaRepository<TraceOrgSupernetCategory,Long>
{
    TraceOrgSupernetCategory findByCategoryName(String supernetWithCidr);

}
