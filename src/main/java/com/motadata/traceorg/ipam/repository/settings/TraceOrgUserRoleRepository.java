package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgRoleFeaturePermission;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgUserRoleRepository extends JpaRepository<TraceOrgUserRole, Long> {

    List<TraceOrgRoleFeaturePermission> findRoleFeaturePermissionsByRole(String role);

    boolean existsByRole(String role);
}
