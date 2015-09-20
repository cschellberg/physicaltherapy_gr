package com.agileapps.pt.pojos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.exceptions.TemplateConfigurationException;

import android.util.Log;

@Root
public class FormTemplate {

	public static final String TITLE_DELIMITER = "%%";

	public static final String LINE_DELIMITER = "||";

	public static final String QUESTION_DELIMITER = "&&";

	private static final String NONE = "NONE";

	private static final String KEY_DELIMITER = "_";

	private Map<Integer, QuestionAnswer> widgetIdMap;

	@Element
	private int id;

	@Element(required=false)
	private Boolean permanent=Boolean.FALSE;
	
	@Element(required = false)
	private String clientId;

	@Element(required = false)
	private String dateString;

	@Element
	private String formName;

	@ElementList
	private List<FormTemplatePart> formTemplatePartList;
	
	private String fileName;

	public FormTemplate()
	{
		
	}
	
	public FormTemplate(FormTemplate firstTemplate,FormTemplate secondTemplate) {
		this.formTemplatePartList=new ArrayList<FormTemplatePart>();
		this.formTemplatePartList.addAll(firstTemplate.formTemplatePartList);
		this.formTemplatePartList.addAll(secondTemplate.formTemplatePartList);
		this.setClientId(secondTemplate.clientId);
		this.setFormName(secondTemplate.formName);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Boolean getPermanent() {
		return permanent;
	}

	public void setPermanent(Boolean permanent) {
		this.permanent = permanent;
	}

	public List<FormTemplatePart> getFormTemplatePartList() {
		return formTemplatePartList;
	}

	public void setFormTemplatePartList(
			List<FormTemplatePart> formTemplatePartList) {
		this.formTemplatePartList = formTemplatePartList;
	}

	@Override
	public String toString() {
		return "FormTemplate [id=" + id + ", clientId=" + clientId
				+ ", dateString=" + dateString + ", formName=" + formName
				+ ", formTemplatePartList=" + formTemplatePartList + "]";
	}

	public void clear(boolean all) {
		int cntr=0;
		for (FormTemplatePart formTemplatePart : formTemplatePartList) {
			if ( all || cntr > 0)
			{
			formTemplatePart.clear();
			}
			cntr++;
		}
	}

	public synchronized QuestionAnswer getQuestionAnswer(int widgetId) {
		if (this.widgetIdMap == null) {
			initializeWidgetIdMap();
		}
		QuestionAnswer questionAnswer = widgetIdMap.get(widgetId);
		if (questionAnswer == null) {
			Log.i(MainActivity.PT_APP_INFO,
					"Can find question answer for widget " + widgetId
							+ " re-initializing map");
			initializeWidgetIdMap();// re-initialize the map again to see if you
									// can find it.
			questionAnswer = widgetIdMap.get(widgetId);
		}
		return questionAnswer;
	}

	private void initializeWidgetIdMap() {
		widgetIdMap = new HashMap<Integer, QuestionAnswer>();
		for (FormTemplatePart formTemplatePart : this.formTemplatePartList) {
			for (QuestionAnswer questionAnswer : formTemplatePart
					.getQuestionAnswerList()) {
				for (Integer widgetId : questionAnswer.getWidgetIds()) {
					widgetIdMap.put(widgetId, questionAnswer);
				}
			}
		}
	}

	public String getPrintableString() {
		StringBuilder sb = new StringBuilder();
		for (FormTemplatePart part : this.formTemplatePartList) {
			StringBuilder subSb = new StringBuilder();
			for (QuestionAnswer questionAnswer : part.getQuestionAnswerList()) {
				if (StringUtils.isNotBlank(questionAnswer.getAnswer())) {
					subSb.append(questionAnswer.getQuestion().trim())
							.append(QUESTION_DELIMITER)
							.append(questionAnswer.getAnswer().trim())
							.append(LINE_DELIMITER);
				}
			}
			String questionAnswers = subSb.toString();
			if (StringUtils.isNotEmpty(questionAnswers)) {
				sb.append(part.getTitle()).append(TITLE_DELIMITER)
						.append(LINE_DELIMITER).append(questionAnswers);
			}
		}
		return sb.toString();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getKey() {
		/*
		 * generates a key based on the question answers that have a key index
		 * greater than 0, only the first template part will contain a key all
		 * other parts will be ignored
		 */
		StringBuilder sb = new StringBuilder();
		List<QuestionAnswer> questionAnswerList = new ArrayList<QuestionAnswer>();
		for (QuestionAnswer questionAnswer : this.formTemplatePartList.get(0)
				.getQuestionAnswerList()) {
			if (questionAnswer.getKeyIndex() >= 0) {
				boolean isInserted = false;
				int cntr = 0;
				for (QuestionAnswer keyQuestionAnswer : questionAnswerList) {
					if (keyQuestionAnswer.getKeyIndex() == questionAnswer
							.getKeyIndex()) {
						throw new TemplateConfigurationException(
								"Two questions have the same index, please revise the template!");
					}
					if (questionAnswer.getKeyIndex() < keyQuestionAnswer
							.getKeyIndex()) {
						questionAnswerList.add(cntr, questionAnswer);
						isInserted = true;
						break;
					}
					cntr++;
				}
				if (!isInserted) {
					questionAnswerList.add(questionAnswer);
				}
			}
		}
		int cntr = 0;
		for (QuestionAnswer questionAnswer : questionAnswerList) {
			String keyDelimiter = KEY_DELIMITER;
			if (cntr >= (questionAnswerList.size() - 1)) {
				/* this is last in the last, dont append a delimiter */
				keyDelimiter = "";
			}
			if (StringUtils.isBlank(questionAnswer.getAnswer())) {
				sb.append(NONE).append(keyDelimiter);
			} else {
				sb.append(
						stripNonAlphanumbericCharacters(questionAnswer
								.getAnswer())).append(keyDelimiter);
			}
			cntr++;
		}
		return sb.toString();
	}

	private String stripNonAlphanumbericCharacters(String inStr) {
		if (StringUtils.isBlank(inStr)) {
			return "";
		}
		inStr = inStr.toUpperCase().trim();
		StringBuilder sb = new StringBuilder();
		for (int ii = 0; ii < inStr.length(); ii++) {
			char ch = inStr.charAt(ii);
			if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

}
