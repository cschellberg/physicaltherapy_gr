package com.agileapps.pt.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.util.Log;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.exceptions.TemplateConfigurationException;
import com.agileapps.pt.pojos.Config;
import com.agileapps.pt.pojos.FormTemplate;
import com.agileapps.pt.tasks.TemplateDownloadTask;
import com.agileapps.pt.util.PhysicalTherapyUtils;

public class FormTemplateManager {

	public final static String DEFAULT_CLIENT_INFO_FORM_NAME = "Default Client Info";
	public final static String DEFAULT_FORM_NAME = "Default";
	private final static String DEFAULT_CLIENT_INFO_TEMPLATE = "/assets/DefaultClientInfoTemplate.xml";
	private final static String DEFAULT_FORM_TEMPLATE = "/assets/DefaultFormTemplate.xml";
	private static Map<String, FormTemplate>formTemplateMap;
	public static String temporaryFormTemplateOverride=null;
	private static FormTemplate currentFormTemplate=null;
	
		
	
	public static void  initFormTemplateMap() throws Exception
	{
		formTemplateMap = new HashMap<String, FormTemplate>();
		FormTemplate defaultClientInfoTemplate = PhysicalTherapyUtils
				.parseFormTemplate(FormTemplateManager.class
						.getResourceAsStream(DEFAULT_CLIENT_INFO_TEMPLATE));
		FormTemplate defaultFormTemplate = PhysicalTherapyUtils
				.parseFormTemplate(FormTemplateManager.class
						.getResourceAsStream(DEFAULT_FORM_TEMPLATE));
		formTemplateMap.put(DEFAULT_CLIENT_INFO_FORM_NAME, defaultClientInfoTemplate);
		formTemplateMap.put(DEFAULT_FORM_NAME, defaultFormTemplate);
			File templatesDir = new File(Environment.getExternalStorageDirectory(),
					TemplateDownloadTask.TEMPLATES);
			if ( ! templatesDir.exists()){
				templatesDir.mkdir();
			}
			for ( File file:templatesDir.listFiles()){
				if (file.getName().endsWith("xml")){
					try
					{
					FormTemplate formTemplate = PhysicalTherapyUtils
							.parseFormTemplate(new FileInputStream(file));
					formTemplate.setFileName(file.getName());
					formTemplateMap.put(formTemplate.getFormName(), formTemplate);
					}catch(Exception ex){
						Log.e(MainActivity.PT_APP_INFO,"Unable to load template "+file.getName()+" because "+ex);
					}
				}
			}


	}

	private synchronized static void setFormTemplateMap() throws Exception
	{
		if (formTemplateMap == null) {
			initFormTemplateMap();
		}
	}
	
	private static String getFormTemplateName(Config config)
	{
		if (temporaryFormTemplateOverride != null ){
			return temporaryFormTemplateOverride;
		}else{
			return config.getDefaultFormTemplate();
		}
	}
	
	public static FormTemplate getFormTemplate() throws Exception {
		Config config=ConfigManager.getConfig();
		return getFormTemplate(config.getDefaultClientInfoTemplate(),getFormTemplateName(config));
	}

	public static synchronized FormTemplate getFormTemplate(
			String clientInfoTemplateName, String formTemplateName)
			throws Exception {
		setFormTemplateMap();
		if ( currentFormTemplate == null || !currentFormTemplate.getFormName().equals(formTemplateName))
		{	
		FormTemplate clientInfoTemplate = formTemplateMap.get(clientInfoTemplateName);
		if (clientInfoTemplate == null) {
			throw new TemplateConfigurationException(
					"No template found with name "
							+ clientInfoTemplateName);
		}
		FormTemplate formTemplate = formTemplateMap.get(formTemplateName);
		if (formTemplate == null) {
			throw new TemplateConfigurationException(
					"No template found with name "
							+ formTemplateName);
		}
		currentFormTemplate =new FormTemplate(clientInfoTemplate,formTemplate);
		}
		return currentFormTemplate;
	}

	public static List<String> getClientInfoFormTemplateNames() throws Exception{
		setFormTemplateMap();
		List<String>retList=new ArrayList<String>();
		for ( FormTemplate formTemplate:formTemplateMap.values()){
			if ( formTemplate.getPermanent()){
				retList.add(formTemplate.getFormName());
			}
		}
		return retList;
	}
	
	public static List<String> getFormTemplateNames() throws Exception{
		setFormTemplateMap();
		List<String>retList=new ArrayList<String>();
		for ( FormTemplate formTemplate:formTemplateMap.values()){
			if (!  formTemplate.getPermanent()){
				retList.add(formTemplate.getFormName());
			}
		}
		return retList;
	}
	
	public static void loadForm(File formFile) throws FileNotFoundException,
			Exception {
		FormTemplate formTemplate = PhysicalTherapyUtils
				.parseFormTemplate(new FileInputStream(formFile));
		temporaryFormTemplateOverride=formTemplate.getFormName();
		currentFormTemplate=formTemplate;

	}

	public static void deleteTemplate(String formName)
	{
		FormTemplate formTemplate=formTemplateMap.get(formName);
		if ( formTemplate == null  || formTemplate.getFileName() == null){
			return;
		}
		File templatesDir = new File(Environment.getExternalStorageDirectory(),
				TemplateDownloadTask.TEMPLATES);
		File templateFile=new File(templatesDir,formTemplate.getFileName());
		if ( templateFile.delete()){
			formTemplateMap.remove(formName);
		}
	}
}
