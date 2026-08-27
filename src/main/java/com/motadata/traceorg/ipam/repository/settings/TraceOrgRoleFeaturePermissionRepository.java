package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgRoleFeaturePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceOrgRoleFeaturePermissionRepository extends JpaRepository<TraceOrgRoleFeaturePermission, Long> {

    List<TraceOrgRoleFeaturePermission> findByRole(Long role);

    void deleteByRole(Long role);
}
