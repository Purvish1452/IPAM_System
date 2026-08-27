package com.motadata.traceorg.ipam.entity.settings;


import javax.persistence.*;

@Entity
@Table(name = "role_feature_permission")
public class TraceOrgRoleFeaturePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Long role; // Link to TraceOrgUserRole (role)

    @ManyToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private TraceOrgFeature feature; // Link to Feature entity

    @Column(nullable = false)
    private boolean readPermission; // Permission for read access

    @Column(nullable = false)
    private boolean writePermission; // Permission for write access

    public TraceOrgFeature getFeature() {
        return feature;
    }

    public void setFeature(TraceOrgFeature feature) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }
}
