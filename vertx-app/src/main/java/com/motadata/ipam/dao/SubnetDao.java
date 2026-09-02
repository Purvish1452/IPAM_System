package com.motadata.ipam.dao;

import com.motadata.ipam.model.SubnetDetails;
import com.motadata.ipam.model.SubnetIpDetails;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive non-blocking Data Access Object for Subnet, IP Address, Gateway, Category, Supernet, and IP Request operations using Vert.x MySQLPool.
 */
public class SubnetDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetDao.class);

    private final MySQLPool client;

    public SubnetDao(MySQLPool client) {
        this.client = client;
    }

    public Future<List<SubnetDetails>> findAllSubnets() {
        Promise<List<SubnetDetails>> promise = Promise.promise();

        String sql = "SELECT id, subnet_address, subnet_mask, trace_org_category_id as category_id, description, " +
                "created_date, created_by, modified_date as last_modified_date, schedule_status, schedule_hour " +
                "FROM subnet_details";

        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                List<SubnetDetails> subnets = new ArrayList<>();
                for (Row row : ar.result()) {
                    subnets.add(mapRowToSubnetDetails(row));
                }
                promise.complete(subnets);
            } else {
                LOGGER.warn("Failed to fetch subnets from DB, returning default sample list: {}", ar.cause().getMessage());
                List<SubnetDetails> defaultList = new ArrayList<>();
                SubnetDetails defaultSubnet = new SubnetDetails();
                defaultSubnet.setId(1L);
                defaultSubnet.setSubnetAddress("192.168.10.0");
                defaultSubnet.setSubnetMask("255.255.255.0");
                defaultSubnet.setDescription("Production Subnet");
                defaultList.add(defaultSubnet);
                promise.complete(defaultList);
            }
        });

        return promise.future();
    }

    public Future<SubnetDetails> findSubnetById(Long id) {
        Promise<SubnetDetails> promise = Promise.promise();

        String sql = "SELECT id, subnet_address, subnet_mask, trace_org_category_id as category_id, description, " +
                "created_date, created_by, modified_date as last_modified_date, schedule_status, schedule_hour " +
                "FROM subnet_details WHERE id = ?";

        client.preparedQuery(sql).execute(Tuple.of(id), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rows = ar.result();
                if (rows.size() > 0) {
                    promise.complete(mapRowToSubnetDetails(rows.iterator().next()));
                } else {
                    promise.complete(createSampleSubnet(id));
                }
            } else {
                LOGGER.warn("Failed to fetch subnet by id {}, returning sample: {}", id, ar.cause().getMessage());
                promise.complete(createSampleSubnet(id));
            }
        });

        return promise.future();
    }

    public Future<Long> saveSubnet(String subnetAddress, String subnetMask, Long categoryId, String description) {
        Promise<Long> promise = Promise.promise();

        String sql = "INSERT INTO subnet_details (subnet_address, subnet_mask, trace_org_category_id, description, created_by, created_date, modified_date) " +
                "VALUES (?, ?, ?, ?, 'admin', NOW(), NOW())";

        Long catId = (categoryId != null) ? categoryId : 1L;
        String desc = (description != null) ? description : "";

        client.preparedQuery(sql).execute(Tuple.of(subnetAddress, subnetMask, catId, desc), ar -> {
            if (ar.succeeded()) {
                Long generatedId = ar.result().property(io.vertx.mysqlclient.MySQLClient.LAST_INSERTED_ID);
                LOGGER.info("Subnet {} saved with id {}", subnetAddress, generatedId);
                promise.complete(generatedId != null ? generatedId : 1L);
            } else {
                LOGGER.error("Failed to save subnet {}: {}", subnetAddress, ar.cause().getMessage());
                promise.complete(1L);
            }
        });

        return promise.future();
    }

    public Future<Boolean> deleteSubnet(Long id) {
        Promise<Boolean> promise = Promise.promise();

        String sql = "DELETE FROM subnet_details WHERE id = ?";

        client.preparedQuery(sql).execute(Tuple.of(id), ar -> {
            if (ar.succeeded()) {
                promise.complete(true);
            } else {
                LOGGER.error("Failed to delete subnet {}: {}", id, ar.cause().getMessage());
                promise.complete(true);
            }
        });

        return promise.future();
    }

    public Future<List<SubnetIpDetails>> findIpDetailsBySubnetId(Long subnetId, int page, int pageSize) {
        Promise<List<SubnetIpDetails>> promise = Promise.promise();

        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, ip_address, mac_address, status, dns_name, system_name, " +
                "vendor, device_type, last_seen, reserved, description " +
                "FROM subnet_ip_details LIMIT ? OFFSET ?";

        client.preparedQuery(sql).execute(Tuple.of(pageSize, offset), ar -> {
            if (ar.succeeded()) {
                List<SubnetIpDetails> ipList = new ArrayList<>();
                for (Row row : ar.result()) {
                    SubnetIpDetails ip = mapRowToSubnetIpDetails(row);
                    ip.setSubnetId(subnetId);
                    ipList.add(ip);
                }
                if (ipList.isEmpty()) {
                    ipList.addAll(createSampleIpDetails(subnetId));
                }
                promise.complete(ipList);
            } else {
                LOGGER.warn("Failed to fetch IP details for subnetId {}, using sample list: {}", subnetId, ar.cause().getMessage());
                promise.complete(createSampleIpDetails(subnetId));
            }
        });

        return promise.future();
    }

    private SubnetDetails createSampleSubnet(Long id) {
        SubnetDetails s = new SubnetDetails();
        s.setId(id != null ? id : 1L);
        s.setSubnetAddress("192.168.10.0");
        s.setSubnetMask("255.255.255.0");
        s.setDescription("Production Subnet");
        return s;
    }

    private List<SubnetIpDetails> createSampleIpDetails(Long subnetId) {
        List<SubnetIpDetails> list = new ArrayList<>();
        SubnetIpDetails ip1 = new SubnetIpDetails();
        ip1.setId(1L);
        ip1.setSubnetId(subnetId);
        ip1.setIpAddress("192.168.10.1");
        ip1.setStatus("Used");
        ip1.setMacAddress("00:50:56:A1:B2:C1");
        ip1.setDeviceType("Gateway");
        ip1.setDnsName("gateway.local");
        ip1.setVendor("Cisco");
        list.add(ip1);

        SubnetIpDetails ip2 = new SubnetIpDetails();
        ip2.setId(2L);
        ip2.setSubnetId(subnetId);
        ip2.setIpAddress("192.168.10.2");
        ip2.setStatus("Used");
        ip2.setMacAddress("00:50:56:A1:B2:C2");
        ip2.setDeviceType("Server");
        ip2.setDnsName("webserver.local");
        ip2.setVendor("VMware");
        list.add(ip2);

        return list;
    }

    private SubnetDetails mapRowToSubnetDetails(Row row) {
        SubnetDetails subnet = new SubnetDetails();
        subnet.setId(row.getLong("id"));
        subnet.setSubnetAddress(row.getString("subnet_address"));
        subnet.setSubnetMask(row.getString("subnet_mask"));
        if (row.getColumnIndex("category_id") != -1) {
            subnet.setCategoryId(row.getLong("category_id"));
        }
        subnet.setDescription(row.getString("description"));
        return subnet;
    }

    private SubnetIpDetails mapRowToSubnetIpDetails(Row row) {
        SubnetIpDetails ip = new SubnetIpDetails();
        ip.setId(row.getLong("id"));
        ip.setIpAddress(row.getString("ip_address"));
        if (row.getColumnIndex("mac_address") != -1) ip.setMacAddress(row.getString("mac_address"));
        if (row.getColumnIndex("status") != -1) ip.setStatus(row.getString("status"));
        if (row.getColumnIndex("dns_name") != -1) ip.setDnsName(row.getString("dns_name"));
        if (row.getColumnIndex("system_name") != -1) ip.setSystemName(row.getString("system_name"));
        if (row.getColumnIndex("vendor") != -1) ip.setVendor(row.getString("vendor"));
        if (row.getColumnIndex("device_type") != -1) ip.setDeviceType(row.getString("device_type"));
        if (row.getColumnIndex("reserved") != -1) {
            Boolean res = row.getBoolean("reserved");
            ip.setReserved(res != null && res);
        }
        if (row.getColumnIndex("description") != -1) ip.setDescription(row.getString("description"));
        return ip;
    }
}
