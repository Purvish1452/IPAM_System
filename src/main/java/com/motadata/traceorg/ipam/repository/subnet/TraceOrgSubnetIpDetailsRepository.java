package com.motadata.traceorg.ipam.repository.subnet;

import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Repository
public interface TraceOrgSubnetIpDetailsRepository extends JpaRepository<TraceOrgSubnetIpDetails, Long>
{
    TraceOrgSubnetIpDetails findByMacAddressAndIpAddress(String macAddress, String ipAddress);

    @Query("SELECT t.dnsStatus, COUNT(t) FROM TraceOrgSubnetIpDetails t GROUP BY t.dnsStatus")
    List<Object[]> findGroupedByDnsStatus();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE TraceOrgSubnetIpDetails t SET t.status = :status WHERE t.ipAddress IN :ipAddresses")
    int updateStatusByIpAddresses(@Param("status") String status, @Param("ipAddresses") List<String> ipAddresses);

    @Query("SELECT COUNT(t) > 0 FROM TraceOrgSubnetIpDetails t WHERE t.ipAddress IN :ipAddresses AND t.status = 'Available'")
    boolean existsAvailableStatus(@Param("ipAddresses") List<String> ipAddresses);

    @Query(value = "SELECT * FROM subnet_ip_details WHERE deactive_status = false AND subnet_id_id = :subnetId ORDER BY INET_ATON(ip_address)", nativeQuery = true)
    List<TraceOrgSubnetIpDetails> findActiveSubnetIpsOrdered(@Param("subnetId") Long subnetId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE subnet_ip_details " +
            "SET custom_columns = JSON_REMOVE(custom_columns, CONCAT('$.', :key)) " +
            "WHERE JSON_CONTAINS_PATH(custom_columns, 'one', CONCAT('$.', :key)) " +
            "AND id IS NOT NULL",
            nativeQuery = true)
    void removeKeyFromCustomColumns(@Param("key") String keyToRemove);


    @Modifying
    @Transactional
    @Query(value = "UPDATE subnet_ip_details " +
            "SET custom_columns = JSON_SET(JSON_REMOVE(custom_columns, :oldKey), :newKey, JSON_UNQUOTE(JSON_EXTRACT(custom_columns, :oldKey))) " +
            "WHERE JSON_CONTAINS_PATH(custom_columns, 'one', :oldKey)", nativeQuery = true)
    void renameKeyInCustomColumns(@Param("oldKey") String oldKey, @Param("newKey") String newKey);


    List<TraceOrgSubnetIpDetails> findByIpAddressIn(List<String> ipAddresses);
}
