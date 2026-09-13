package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String password;
    private String email;
    private UserRole userRoleId;
    private Long roleId;
    private String activeStatus;
    private Date currentLoginStatus;
    private Date previousLoginStatus;
    private boolean status = true;
    private String description;

    // Default constructor for User
    public User() {
    }

    // Parameterized constructor initializing ID, username, email, and active status
    public User(Long id, String userName, String email, boolean status) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.status = status;
    }

    // Gets the user ID
    public Long getId() {
        return id;
    }

    // Sets the user ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the username
    public String getUserName() {
        return userName;
    }

    // Sets the username
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Gets the hashed user password
    public String getPassword() {
        return password;
    }

    // Sets the hashed user password
    public void setPassword(String password) {
        this.password = password;
    }

    // Gets the user email address
    public String getEmail() {
        return email;
    }

    // Sets the user email address
    public void setEmail(String email) {
        this.email = email;
    }

    // Gets the nested UserRole object
    public UserRole getUserRoleId() {
        return userRoleId;
    }

    // Sets the nested UserRole object
    public void setUserRoleId(UserRole userRoleId) {
        this.userRoleId = userRoleId;
    }

    // Gets the role foreign key ID
    public Long getRoleId() {
        return roleId;
    }

    // Sets the role foreign key ID
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    // Gets the textual active status (e.g. Active, Inactive)
    public String getActiveStatus() {
        return activeStatus;
    }

    // Sets the textual active status
    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    // Gets the current login timestamp
    public Date getCurrentLoginStatus() {
        return currentLoginStatus;
    }

    // Sets the current login timestamp
    public void setCurrentLoginStatus(Date currentLoginStatus) {
        this.currentLoginStatus = currentLoginStatus;
    }

    // Gets the previous login timestamp
    public Date getPreviousLoginStatus() {
        return previousLoginStatus;
    }

    // Sets the previous login timestamp
    public void setPreviousLoginStatus(Date previousLoginStatus) {
        this.previousLoginStatus = previousLoginStatus;
    }

    // Checks whether the user account is enabled
    public boolean isStatus() {
        return status;
    }

    // Sets whether the user account is enabled
    public void setStatus(boolean status) {
        this.status = status;
    }

    // Gets the user description / full name / notes
    public String getDescription() {
        return description;
    }

    // Sets the user description / full name / notes
    public void setDescription(String description) {
        this.description = description;
    }
}
