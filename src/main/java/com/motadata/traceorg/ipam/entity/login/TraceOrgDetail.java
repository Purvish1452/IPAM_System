package com.motadata.traceorg.ipam.entity.login;

/**
 * @author Krunal Thakkar
 *
 */

public class TraceOrgDetail
{

	public String tokenValue;

	public String getTokenValue() {

		if(tokenValue!=null){
			return tokenValue.trim();
		}
		return tokenValue;
	}
	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue.trim();
	}
}
