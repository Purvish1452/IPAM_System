package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraceOrgUserRepository extends JpaRepository<TraceOrgUser, Long>
{
    boolean existsByUserName(String userName);

    Optional<TraceOrgUser> findByUserName(String userName);

    List<TraceOrgUser> findByUserRoleId_Id(Long roleId);

}
