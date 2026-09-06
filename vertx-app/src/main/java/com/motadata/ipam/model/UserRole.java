package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String role;
    private String description;
    private List<RoleFeaturePermission> roleFeaturePermissions = new ArrayList<>();

    public UserRole() {
    }

    public UserRole(Long id, String role, String description) {
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RoleFeaturePermission> getRoleFeaturePermissions() {
        return roleFeaturePermissions;
    }

    public void setRoleFeaturePermissions(List<RoleFeaturePermission> roleFeaturePermissions) {
        this.roleFeaturePermissions = roleFeaturePermissions;
    }
}
