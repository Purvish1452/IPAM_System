package com.motadata.traceorg.ipam.services.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;

import java.util.HashMap;

public interface TraceOrgUserService
{
    HashMap<String, Object> listAllUsers();

    HashMap<String, Object> insertUser(TraceOrgUser traceOrgUser, String header);

    HashMap<String, Object> getUser(Long id);

    HashMap<String, Object> updateUser(Long id, TraceOrgUser traceOrgUser, String header);

    HashMap<String, Object> removeUser(Long id, String header);

    HashMap<String, Object> listAllUserRoles();

    HashMap<String, Object> changePassword(Long id, TraceOrgUser traceOrgUser);
}
