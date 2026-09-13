package com.motadata.ipam.model;

import java.io.Serializable;

public class RoleFeaturePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;
    private Long featureId;
    private Feature feature;
    private boolean readPermission;
    private boolean writePermission;

    // Default constructor for RoleFeaturePermission
    public RoleFeaturePermission() {
    }

    // Parameterized constructor initializing role permissions
    public RoleFeaturePermission(Long id, Long roleId, Feature feature, boolean readPermission, boolean writePermission) {
        this.id = id;
        this.roleId = roleId;
        this.feature = feature;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }

    // Gets the permission mapping ID
    public Long getId() {
        return id;
    }

    // Sets the permission mapping ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the associated role ID
    public Long getRoleId() {
        return roleId;
    }

    // Sets the associated role ID
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    // Gets the associated feature ID
    public Long getFeatureId() {
        return featureId;
    }

    // Sets the associated feature ID
    public void setFeatureId(Long featureId) {
        this.featureId = featureId;
    }

    // Gets the associated Feature object
    public Feature getFeature() {
        return feature;
    }

    // Sets the associated Feature object
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    // Checks if read permission is granted
    public boolean isReadPermission() {
        return readPermission;
    }

    // Sets the read permission flag
    public void setReadPermission(boolean readPermission) {
        this.readPermission = readPermission;
    }

    // Checks if write permission is granted
    public boolean isWritePermission() {
        return writePermission;
    }

    // Sets the write permission flag
    public void setWritePermission(boolean writePermission) {
        this.writePermission = writePermission;
    }
}
