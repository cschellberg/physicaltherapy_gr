package com.agileapps.pt.pojos;

public class StatusAndResponse {
	public final int statusCode;
	public final String message;

	public StatusAndResponse(int statusCode, String message) {
		this.statusCode=statusCode;
		this.message = message;
	}

	@Override
	public String toString() {
		return "StatusAndResponse{" +
				"statusCode=" + statusCode +
				", message='" + message + '\'' +
				'}';
	}
}
