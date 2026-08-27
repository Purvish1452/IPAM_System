package com.motadata.traceorg.ipam.repository.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
 * Jpa Repository for custom columns.
 */
@Repository
public interface TraceOrgCustomColumnRepository extends JpaRepository<TraceOrgCustomColumn, Long> {

    List<TraceOrgCustomColumn> findByColumnAt(String columnAt);

    boolean existsByColumnName(String columnName);

}
