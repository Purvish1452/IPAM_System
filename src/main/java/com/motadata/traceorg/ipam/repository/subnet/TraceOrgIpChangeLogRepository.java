package com.motadata.traceorg.ipam.repository.subnet;

import com.motadata.traceorg.ipam.entity.subnet.TraceOrgIPChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TraceOrgIpChangeLogRepository extends JpaRepository<TraceOrgIPChangeLog, Long>
{
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ip_change_log WHERE DATEDIFF(:currentDate, timestamp) > :maintainedDays", nativeQuery = true)
    void deleteIpChangeLogs(@Param("currentDate") String currentDate, @Param("maintainedDays") int maintainedDays);

}
