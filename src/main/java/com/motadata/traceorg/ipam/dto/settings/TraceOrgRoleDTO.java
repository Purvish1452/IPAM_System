package com.motadata.traceorg.ipam.dto.settings;

import java.util.List;

public class TraceOrgRoleDTO {

    private Long id;
    private String roleName;
    private String description;
    private List<TraceOrgPermissionDTO> permissions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getters and Setters
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TraceOrgPermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<TraceOrgPermissionDTO> permissions) {
        this.permissions = permissions;
    }
}
