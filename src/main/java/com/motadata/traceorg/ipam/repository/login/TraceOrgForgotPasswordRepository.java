package com.motadata.traceorg.ipam.repository.login;

import com.motadata.traceorg.ipam.entity.login.TraceOrgForgotPassword;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TraceOrgForgotPasswordRepository extends JpaRepository<TraceOrgForgotPassword, Long>
{
    @Transactional
    void deleteByUser(TraceOrgUser traceOrgUser);
}
