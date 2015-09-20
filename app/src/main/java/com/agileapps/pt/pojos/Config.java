package com.agileapps.pt.pojos;

import org.simpleframework.xml.Element;

import com.agileapps.pt.manager.FormTemplateManager;

public class Config {

	private static final String TEMPLATE_URL = "http://pajavaapps.com/pt/";

	@Element
	private String company;

	@Element
	private String defaultClientInfoTemplate = FormTemplateManager.DEFAULT_CLIENT_INFO_FORM_NAME;

	@Element
	private String defaultFormTemplate = FormTemplateManager.DEFAULT_FORM_NAME;

	@Element
	private String templateURL=TEMPLATE_URL;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDefaultClientInfoTemplate() {
		return defaultClientInfoTemplate;
	}

	public void setDefaultClientInfoTemplate(String defaultClientInfoTemplate) {
		this.defaultClientInfoTemplate = defaultClientInfoTemplate;
	}

	public String getDefaultFormTemplate() {
		return defaultFormTemplate;
	}

	public void setDefaultFormTemplate(String defaultFormTemplate) {
		this.defaultFormTemplate = defaultFormTemplate;
	}

	public String getTemplateURL() {
		return templateURL;
	}

	public void setTemplateURL(String templateURL) {
		this.templateURL = templateURL;
	}

}
