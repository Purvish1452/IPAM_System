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
 * Reactive non-blocking Data Access Object for Subnet and IP Address operations using Vert.x MySQLPool.
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
                LOGGER.error("Failed to fetch all subnets: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
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
                    promise.complete(null);
                }
            } else {
                LOGGER.error("Failed to fetch subnet by id {}: {}", id, ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    public Future<List<SubnetIpDetails>> findIpDetailsBySubnetId(Long subnetId, int page, int pageSize) {
        Promise<List<SubnetIpDetails>> promise = Promise.promise();

        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, trace_org_subnet_details_id as subnet_id, ip_address, mac_address, status, dns_name, system_name, " +
                "vendor, device_type, last_seen, reserved, description " +
                "FROM subnet_ip_details WHERE trace_org_subnet_details_id = ? LIMIT ? OFFSET ?";

        client.preparedQuery(sql).execute(Tuple.of(subnetId, pageSize, offset), ar -> {
            if (ar.succeeded()) {
                List<SubnetIpDetails> ipList = new ArrayList<>();
                for (Row row : ar.result()) {
                    ipList.add(mapRowToSubnetIpDetails(row));
                }
                promise.complete(ipList);
            } else {
                LOGGER.error("Failed to fetch IP details for subnetId {}: {}", subnetId, ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    private SubnetDetails mapRowToSubnetDetails(Row row) {
        SubnetDetails subnet = new SubnetDetails();
        subnet.setId(row.getLong("id"));
        subnet.setSubnetAddress(row.getString("subnet_address"));
        subnet.setSubnetMask(row.getString("subnet_mask"));
        subnet.setCategoryId(row.getLong("category_id"));
        subnet.setDescription(row.getString("description"));
        subnet.setCreatedBy(row.getString("created_by"));
        return subnet;
    }

    private SubnetIpDetails mapRowToSubnetIpDetails(Row row) {
        SubnetIpDetails ip = new SubnetIpDetails();
        ip.setId(row.getLong("id"));
        ip.setSubnetId(row.getLong("subnet_id"));
        ip.setIpAddress(row.getString("ip_address"));
        ip.setMacAddress(row.getString("mac_address"));
        ip.setStatus(row.getString("status"));
        ip.setDnsName(row.getString("dns_name"));
        ip.setSystemName(row.getString("system_name"));
        ip.setVendor(row.getString("vendor"));
        ip.setDeviceType(row.getString("device_type"));
        ip.setReserved(row.getBoolean("reserved") != null && row.getBoolean("reserved"));
        ip.setDescription(row.getString("description"));
        return ip;
    }
}
