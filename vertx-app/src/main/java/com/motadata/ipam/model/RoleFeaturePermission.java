package com.motadata.ipam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleFeaturePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;
    private Long featureId;
    private Feature feature;
    private boolean readPermission;
    private boolean writePermission;

    public RoleFeaturePermission() {
    }

    public RoleFeaturePermission(Long id, Long roleId, Feature feature, boolean readPermission, boolean writePermission) {
        this.id = id;
        this.roleId = roleId;
        this.feature = feature;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(Long featureId) {
        this.featureId = featureId;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isReadPermission() {
        return readPermission;
    }

    public void setReadPermission(boolean readPermission) {
        this.readPermission = readPermission;
    }

    public boolean isWritePermission() {
        return writePermission;
    }

    public void setWritePermission(boolean writePermission) {
        this.writePermission = writePermission;
    }
}
