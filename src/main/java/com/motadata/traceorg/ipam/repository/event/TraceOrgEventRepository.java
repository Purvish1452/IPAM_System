package com.motadata.traceorg.ipam.repository.event;

import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TraceOrgEventRepository extends JpaRepository<TraceOrgEvent, Long>
{

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM event WHERE id IN (SELECT id FROM event WHERE DATEDIFF(:currentDate, timestamp) > :maintainedDays)", nativeQuery = true)
    void deleteEvents(@Param("currentDate") String currentDate, @Param("maintainedDays") int maintainedDays);

    @Transactional
    void deleteByDoneBy(TraceOrgUser doneBy);
}
