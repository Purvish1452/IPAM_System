package com.motadata.ipam.service;

import com.motadata.ipam.dao.SubnetDao;
import com.motadata.ipam.model.SubnetDetails;
import com.motadata.ipam.model.SubnetIpDetails;
import io.vertx.core.Future;

import java.util.List;

/**
 * Asynchronous Vert.x service for Subnet and IP Address management operations.
 */
public class SubnetService {

    private final SubnetDao subnetDao;

    public SubnetService(SubnetDao subnetDao) {
        this.subnetDao = subnetDao;
    }

    public Future<List<SubnetDetails>> getAllSubnets() {
        return subnetDao.findAllSubnets();
    }

    public Future<SubnetDetails> getSubnetById(Long id) {
        return subnetDao.findSubnetById(id);
    }

    public Future<List<SubnetIpDetails>> getIpDetails(Long subnetId, Integer page, Integer pageSize) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;

        return subnetDao.findIpDetailsBySubnetId(subnetId, pageNum, size);
    }
}
