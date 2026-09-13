package com.motadata.ipam.model;

import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object data;
    private boolean success = true;
    private String message;
    private String currentUserRole;

    // Default constructor for Response
    public Response() {
    }

    // Parameterized constructor initializing payload data and success flag
    public Response(Object data, boolean success) {
        this.data = data;
        this.success = success;
    }

    // Parameterized constructor initializing payload data, success flag, and message
    public Response(Object data, boolean success, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
    }

    // Gets the response payload data
    public Object getData() {
        return data;
    }

    // Sets the response payload data
    public void setData(Object data) {
        this.data = data;
    }

    // Checks whether the response indicates success
    public boolean isSuccess() {
        return success;
    }

    // Sets the response success status
    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Gets the response message
    public String getMessage() {
        return message;
    }

    // Sets the response message
    public void setMessage(String message) {
        this.message = message;
    }

    // Gets the role of the current user for context
    public String getCurrentUserRole() {
        return currentUserRole;
    }

    // Sets the role of the current user
    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }
}
