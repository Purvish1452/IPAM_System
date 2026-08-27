package com.motadata.traceorg.ipam.entity;

/**
 * @author Krunal Thakkar
 *
 */

public class Response {

	private Object data;
	private boolean success;
	private String message;
	private String currentUserRole;
	
	public Response() {
	};

	public Response(Object data, String message, boolean success,String currentUserRole) {
		this.data = data;
		this.message = message;
		this.success = success;
		this.currentUserRole = currentUserRole;
	}

	public String getCurrentUserRole() {
		return currentUserRole;
	}

	public void setCurrentUserRole(String currentUserRole) {
		this.currentUserRole = currentUserRole;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
