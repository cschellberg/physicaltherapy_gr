package com.agileapps.pt.pojos;

import java.util.Arrays;
import java.util.List;

public class TemplateResponse {
	private String result;
	
	private String templates[];

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String[] getTemplates() {
		return templates;
	}

	public void setTemplates(String[] templates) {
		this.templates = templates;
	}

	@Override
	public String toString() {
		return "TemplateResponse [result=" + result + ", templates="
				+ Arrays.toString(templates) + "]";
	}

	
		

}
