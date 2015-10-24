package com.agileapps.pt.util;

import java.io.File;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.agileapps.pt.exceptions.TemplateConfigurationException;
import com.agileapps.pt.pojos.FormTemplate;

public class PhysicalTherapyUtils {

	public static final Object FILE_DELIMITER = "/";
	public static final Object XML_EXTENSION =".xml";
	public static final String DATE_FORMAT = "yyyyMMddHH";

	public static FormTemplate parseFormTemplate(InputStream is)
			throws Exception {
		Serializer serial = new Persister();
		StringBuilder sb = new StringBuilder();
		Scanner scanner=new Scanner(is);
		while (scanner.hasNext()) {
			sb.append(scanner.nextLine().trim()).append("\n");
		}
		FormTemplate formTemplate = serial.read(FormTemplate.class,
				new StringBufferInputStream(sb.toString()));
		return formTemplate;

	}

	public static String getFileName(FormTemplate formTemplate)
	{
		StringBuilder sb=new StringBuilder();
		String clientKey=formTemplate.getKey();
		DateFormat dateFormat=new SimpleDateFormat(DATE_FORMAT);
		String dateString=dateFormat.format(new Date());
		if ( StringUtils.isBlank(formTemplate.getFormName())){
			throw new TemplateConfigurationException("Form has no name.  Please check template to make sure it is specified");
		}
		sb.append(clientKey).append("-").append(formTemplate.getFormName().toUpperCase()).append("-").append(dateString).append(XML_EXTENSION);
		return sb.toString();
	}

	public static File getFilePath(FormTemplate formTemplate)
	{
			StringBuilder sb=new StringBuilder();
			String clientKey=formTemplate.getKey();
			DateFormat dateFormat=new SimpleDateFormat(DATE_FORMAT);
			String dateString=dateFormat.format(new Date());
			if ( StringUtils.isBlank(formTemplate.getFormName())){
				throw new TemplateConfigurationException("Form has no name.  Please check template to make sure it is specified");
			}
			sb.append(FILE_DELIMITER).append(clientKey).append(FILE_DELIMITER)
			.append(formTemplate.getFormName()).append(FILE_DELIMITER)
			.append(dateString).append(FILE_DELIMITER).append(clientKey).append(XML_EXTENSION);
			return new File(sb.toString());
	}
	
	public static String answerReplacer(List<String> valueList,
			String currentAnswer, String newAnswer, boolean isAdd) {
		if ( valueList == null ){
			if ( isAdd){
				return newAnswer;
			}else{
				return currentAnswer;
			}
		}
		if (currentAnswer == null) {
			if (isAdd) {
				return newAnswer;
			} else {
				return null;
			}
		} else if (newAnswer == null) {
			return currentAnswer;
		}
		currentAnswer = currentAnswer.trim();
		newAnswer = newAnswer.trim();
		if (isAdd && currentAnswer.contains(newAnswer)) {
			return currentAnswer; // nothing to do
		} else if (!isAdd && currentAnswer.contains(newAnswer)) {
			String tmpStr = StringUtils.remove(currentAnswer, newAnswer).trim();
			String tmpStrParts[] = tmpStr.split(" ");
			StringBuilder sb = new StringBuilder();
			for (String str : tmpStrParts) {
				sb.append(str).append(" ");
			}
			return sb.toString().trim();
		} else if (isAdd && !currentAnswer.contains(newAnswer)) {
			StringBuilder sb = new StringBuilder();
			String tmpStrParts[] = currentAnswer.split(" ");
			if (tmpStrParts.length == 1 && StringUtils.isBlank(tmpStrParts[0])) {
				return newAnswer;
			}
			Map<String, Integer> valueListMap = new HashMap<String, Integer>();
			int cntr = 0;
			for (String str : valueList) {
				valueListMap.put(str, cntr);
				cntr++;
			}
			int newAnswerIndex = getIndex(valueListMap, newAnswer);
			boolean isAdded = false;
			for (String str : tmpStrParts) {
				int currentAnswerIndex = getIndex(valueListMap, str);
				if (newAnswerIndex < currentAnswerIndex) {
					sb.append(newAnswer).append(" ");
					isAdded=true;
				}
				sb.append(str).append(" ");
			}
			if (!isAdded) {
				sb.append(newAnswer);
			}
			return sb.toString().trim();
		} else {
			// it is a remove but it is not there so nothing to do
			return currentAnswer;
		}
	}

	public static String replaceByLabel( String oldAnswer, String label,String newValue){
		if (StringUtils.isBlank(oldAnswer)  && StringUtils.isNotBlank(newValue) &&  StringUtils.isNotBlank(label)){
			return label+","+newValue+"|";
		}else if (StringUtils.isBlank(newValue) || StringUtils.isBlank(label)){
			return oldAnswer;
		}
		if ( StringUtils.isNotBlank(label) &&! oldAnswer.contains(label)){
			return oldAnswer+label+","+newValue+"|";
		}
		String parts[]=oldAnswer.split("\\|");
		StringBuilder sb=new StringBuilder();
		for ( String part:parts){
			if (part.contains(label)){
				sb.append(label).append(",").append(newValue).append("|");
			}else{
				sb.append(part).append("|");
			}
		}
		return sb.toString();
	}
	
	private static int getIndex(Map<String, Integer> valueMap, String key) {
		int retValue = 100;// totally arbitrary
		if (valueMap.containsKey(key)) {
			retValue = valueMap.get(key);
		}
		return retValue;
	}


	public static int getSelectedIndex(String selectItem, String items[])
	{
		int retValue=0;
		int cntr=0;
		for (String item:items){
			if ( item.equals(selectItem))
			{
				retValue=cntr;
				break;
			}
			cntr++;
		}
		return retValue;
	}

	public static MapOfMaps getMapOfMaps(List<String> stringList){
		MapOfMaps mapOfMaps=new MapOfMaps();
		for (String str:stringList){
			String arr[]=str.split("[-.]");
			arr[arr.length-1]=str;
			mapOfMaps.add(arr);
		}
		return mapOfMaps;
	}
}
