package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Krunal Thakkar
 *
 */

@Table(name = "user_role")
@Entity
public class TraceOrgUserRole {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(length = 20)
	private String role;

	@Column
	private String description;

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<TraceOrgRoleFeaturePermission> roleFeaturePermissions = new HashSet<>();

	public TraceOrgUserRole() {

	}

	public TraceOrgUserRole(TraceOrgUser traceOrgUser, String role) {
		this.role = role;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<TraceOrgRoleFeaturePermission> getRoleFeaturePermissions() {
		return roleFeaturePermissions;
	}

	public void setRoleFeaturePermissions(Set<TraceOrgRoleFeaturePermission> roleFeaturePermissions) {
		this.roleFeaturePermissions = roleFeaturePermissions;
	}

}
