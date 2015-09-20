package com.agileapps.pt.exceptions;

public class TemplateConfigurationException extends RuntimeException {


	public TemplateConfigurationException(String detailMessage) {
		super(detailMessage);
	}

	public TemplateConfigurationException(Throwable throwable) {
		super(throwable);
	}

	public TemplateConfigurationException(String detailMessage,
			Throwable throwable) {
		super(detailMessage, throwable);
	}

}
