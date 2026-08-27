package com.motadata.traceorg.ipam.services.settings;



import com.motadata.traceorg.ipam.dto.settings.TraceOrgFeatureDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgRoleDTO;

import java.util.HashMap;
import java.util.List;


/**
 * IPAM-147
 * IPAM Roadmap : Admin should be able to create Users and should be able to give specific role based access rights to specific user.
 * Added permission based access control
 */
public interface TraceOrgUserRoleService {

    HashMap<String, Object> createRole(TraceOrgRoleDTO roleDTO, String header);

    HashMap<String, Object> listAllRoles();

    HashMap<String, Object> getRole(Long id);

    HashMap<String, Object> updateRole(TraceOrgRoleDTO roleDTO, String header);

    HashMap<String, Object> removeRole(Long id, String header);

    List<TraceOrgFeatureDTO> getAllFeatures();
}
