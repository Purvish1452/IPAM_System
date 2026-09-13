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

    // Default constructor for UserRole
    public UserRole() {
    }

    // Parameterized constructor initializing ID, role name, and description
    public UserRole(Long id, String role, String description) {
        this.id = id;
        this.role = role;
        this.description = description;
    }

    // Gets the user role ID
    public Long getId() {
        return id;
    }

    // Sets the user role ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the role name string
    public String getRole() {
        return role;
    }

    // Sets the role name string
    public void setRole(String role) {
        this.role = role;
    }

    // Gets the role description
    public String getDescription() {
        return description;
    }

    // Sets the role description
    public void setDescription(String description) {
        this.description = description;
    }

    // Gets the list of role-to-feature permissions
    public List<RoleFeaturePermission> getRoleFeaturePermissions() {
        return roleFeaturePermissions;
    }

    // Sets the list of role-to-feature permissions
    public void setRoleFeaturePermissions(List<RoleFeaturePermission> roleFeaturePermissions) {
        this.roleFeaturePermissions = roleFeaturePermissions;
    }
}
