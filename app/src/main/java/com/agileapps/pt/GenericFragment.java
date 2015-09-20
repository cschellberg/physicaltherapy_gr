package com.agileapps.pt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.agileapps.pt.manager.FormTemplateManager;
import com.agileapps.pt.pojos.FormTemplate;
import com.agileapps.pt.pojos.FormTemplatePart;
import com.agileapps.pt.pojos.InputType;
import com.agileapps.pt.pojos.QuestionAnswer;
import com.agileapps.pt.util.PhysicalTherapyUtils;

public abstract class GenericFragment extends Fragment {
	private static final int BOTTOM_MARGIN = 30;
	private static final int LEFT_MARGIN = 80;
	private static final int RIGHT_PADDING = 100;
	protected FormTemplatePart formTemplatePart;
	private int position;

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(MainActivity.PT_APP_INFO, "formTemplate part  "
				+ ((formTemplatePart != null) ? formTemplatePart.getTitle()
						: "null formtemplate part") + " being restored");
		if (formTemplatePart != null) {
			Log.i(MainActivity.PT_APP_INFO,
					"Resetting answers to form template part "
							+ formTemplatePart.getTitle());
			for (QuestionAnswer questionAnswer : formTemplatePart
					.getQuestionAnswerList()) {
				String answer = questionAnswer.getAnswer();
				if (StringUtils.isNotBlank(answer)) {
					Integer widgetIds[] = questionAnswer.getWidgetIds();
					if (widgetIds == null || widgetIds.length == 0) {
						Log.e(MainActivity.PT_APP_INFO,
								"Something went really wrong, question answer was found with no associated widgets");
						continue;
					}
					if (questionAnswer.getInputType() != InputType.CHECKBOX
							&& questionAnswer.getInputType() != InputType.RADIO) {
						View view = this.getActivity().findViewById(
								widgetIds[0]);
						if (view == null) {
							Log.e(MainActivity.PT_APP_INFO,
									"Null view returned for widget id "
											+ widgetIds[0]);
							continue;
						}
						if (!questionAnswer.hasChoiceList()) {
							((EditText) view).setText(answer);
						} else {
							setTextViews((ViewGroup) view, answer);
						}
					} else if (questionAnswer.getInputType() == InputType.RADIO) {
						View view = this.getActivity().findViewById(
								widgetIds[0]);
						if (view == null) {
							Log.e(MainActivity.PT_APP_INFO,
									"Null view returned for widget id "
											+ widgetIds[0]);
							continue;
						}
						RadioGroup radioGroup = (RadioGroup) view;
						for (int ii = 0; ii < radioGroup.getChildCount(); ii++) {
							View subView = radioGroup.getChildAt(ii);
							if (subView instanceof RadioButton) {
								RadioButton radioButton = (RadioButton) subView;
								if (answer.equals(radioButton.getText())) {
									radioButton.setChecked(true);
								} else {
									radioButton.setChecked(false);
								}
							}
						}
					} else if (questionAnswer.getInputType() == InputType.CHECKBOX) {
						for (int widgetId : widgetIds) {
							View view = this.getActivity().findViewById(
									widgetId);
							if (view == null) {
								Log.e(MainActivity.PT_APP_INFO,
										"No view found for widget id "
												+ widgetId);
								continue;
							}
							if (view instanceof CheckBox) {
								CheckBox checkBox = (CheckBox) view;
								if (answer.contains(checkBox.getText())) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							}
						}
					}
				}
			}
		}
	}

	private void setTextViews(ViewGroup viewGroup, String answer) {
		String parts[] = answer.split("\\|");
		Map<String, String> valueMap = new HashMap<String, String>();
		for (String part : parts) {
			String subParts[] = part.split("\\,");
			if (subParts.length > 1) {
				valueMap.put(subParts[0], subParts[1]);
			}
		}
		for (int ii = 0; ii < viewGroup.getChildCount(); ii++) {
			View view = viewGroup.getChildAt(ii);
			if (view instanceof TextView
					&& (ii + 1) < viewGroup.getChildCount()) {
				String value = valueMap.get(((TextView) view).getText());
				if (value != null) {
					View editView = viewGroup.getChildAt(ii + 1);
					if (editView instanceof EditText) {
						((EditText) editView).setText(value);
					}
				}
			}

		}

	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/*String tag = this.getTag();
		String positionStr = tag.substring(MainActivity.FRAGMENT_PREFIX
				.length());
		int position = Integer.parseInt(positionStr.trim());*/
		this.formTemplatePart = getFormTemplate().getFormTemplatePartList()
				.get(position);
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.generic_fragment, container, false);
		ScrollView scrollView = new ScrollView(this.getActivity());
		rootView.addView(scrollView);
		TableLayout tableLayout = new TableLayout(this.getActivity());
		// scrollView.addView(tableLayout);
		scrollView.addView(tableLayout);

		/*
		 * TableLayout tableLayout = (TableLayout) rootView
		 * .findViewById(tableLayoutId);
		 */
		for (final QuestionAnswer questionAnswer : formTemplatePart
				.getQuestionAnswerList()) {
			try {
				TableRow tableRow = new TableRow(this.getActivity());
				tableLayout.addView(tableRow);
				TextView questionView = new TextView(this.getActivity());
				questionView.setText(questionAnswer.getQuestion().trim());
				questionView.setTextSize(25f);
				tableRow.addView(questionView);
				((TableRow.LayoutParams) questionView.getLayoutParams()).leftMargin = 30;
				((TableRow.LayoutParams) questionView.getLayoutParams()).width = 0;
				((TableRow.LayoutParams) questionView.getLayoutParams()).weight = 0.5f;
				if (questionAnswer.getInputType() != InputType.CHECKBOX
						&& questionAnswer.getInputType() != InputType.RADIO) {
					ViewGroup viewGroup = new LinearLayout(this.getActivity());
					if (!questionAnswer.hasChoiceList()) {
						addTextBox(viewGroup, "", tableRow, questionAnswer,
								true);
						tableRow.addView(viewGroup);
					} else {
						int widgetId = MainActivity.getUniqueWidgetId(this
								.getActivity());
						viewGroup.setId(widgetId);
						tableRow.addView(viewGroup);
						for (String prefix : questionAnswer.getChoiceList()) {
							addTextBox(viewGroup, prefix, tableRow,
									questionAnswer, false);
						}
					}
				} else if (questionAnswer.getInputType() == InputType.CHECKBOX) {
					addCheckBox(tableRow, questionAnswer);
				} else {
					addRadio(tableRow, questionAnswer);
				}
			} catch (Exception ex) {
				Log.e(MainActivity.PT_APP_INFO,
						"Unable to initialize fragment because " + ex, ex);
			}
		}
		return rootView;
	}

	private int getRightPadding(int labelLength) {
		int retValue = RIGHT_PADDING - (labelLength * 10);
		return retValue;
	}

	private void addCheckBox(TableRow tableRow,
			final QuestionAnswer questionAnswer) {
		questionAnswer.clearWidgetIds();
		LinearLayout viewGroup = new LinearLayout(this.getActivity());
		for (String value : questionAnswer.getChoiceList()) {
			CheckBox checkBox = new CheckBox(this.getActivity());
			checkBox.setText(value);
			int widgetId = MainActivity.getUniqueWidgetId(this.getActivity());
			checkBox.setId(widgetId);
			viewGroup.addView(checkBox);
			checkBox.setPadding(0, 0, getRightPadding(value.length()), 0);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton compoundButton,
						boolean arg1) {
					CheckBox checkBox = (CheckBox) compoundButton;
					String text = (new StringBuilder()).append(
							checkBox.getText()).toString();
					if (checkBox.isChecked()) {
						String answer = PhysicalTherapyUtils.answerReplacer(
								questionAnswer.getChoiceList(),
								questionAnswer.getAnswer(), text, true);
						questionAnswer.setAnswer(answer.trim());
					} else {
						String oldAnswer = questionAnswer.getAnswer();
						if (oldAnswer == null) {
							oldAnswer = "";
						}
						String answer = PhysicalTherapyUtils.answerReplacer(
								questionAnswer.getChoiceList(), oldAnswer,
								text, false);
						questionAnswer.setAnswer(answer.trim());
					}
				}

			});
			questionAnswer.addWidgetId(widgetId);
		}
		tableRow.addView(viewGroup);
		((TableRow.LayoutParams) viewGroup.getLayoutParams()).bottomMargin = BOTTOM_MARGIN;
		((TableRow.LayoutParams) viewGroup.getLayoutParams()).leftMargin = LEFT_MARGIN;

	}

	private FormTemplate getFormTemplate() {
		FormTemplate formTemplate = null;
		try {
			formTemplate = FormTemplateManager.getFormTemplate();
		} catch (Exception ex) {
			Log.e(MainActivity.PT_APP_INFO,
					"Could not get form template because " + ex);
		}
		return formTemplate;
	}

	private void addRadio(TableRow tableRow, final QuestionAnswer questionAnswer) {
		questionAnswer.clearWidgetIds();
		final RadioGroup radioGroup = new RadioGroup(this.getActivity());
		radioGroup.setOrientation(LinearLayout.HORIZONTAL);
		for (String value : questionAnswer.getChoiceList()) {
			RadioButton radioButton = new RadioButton(this.getActivity());
			radioButton
					.setId(MainActivity.getUniqueWidgetId(this.getActivity()));
			radioButton.setText(value);
			radioGroup.addView(radioButton);
			((RadioGroup.LayoutParams) radioButton.getLayoutParams()).leftMargin = LEFT_MARGIN;
		}
		radioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup radioGoup,
							int radioId) {
						RadioButton radioButton = (RadioButton) radioGroup
								.findViewById(radioId);
						if (radioButton.isChecked()) {
							questionAnswer.setAnswer(String.valueOf(radioButton
									.getText()));
						} else {
							questionAnswer.setAnswer("");
						}
					}

				});
		int widgetId = MainActivity.getUniqueWidgetId(this.getActivity());
		radioGroup.setId(widgetId);
		questionAnswer.addWidgetId(widgetId);
		tableRow.addView(radioGroup);
		((TableRow.LayoutParams) radioGroup.getLayoutParams()).width = 0;
		((TableRow.LayoutParams) radioGroup.getLayoutParams()).weight = 0.5f;
		((TableRow.LayoutParams) radioGroup.getLayoutParams()).gravity = Gravity.LEFT;
		((TableRow.LayoutParams) radioGroup.getLayoutParams()).bottomMargin = BOTTOM_MARGIN;
	}

	private void addTextBox(ViewGroup viewGroup, final String prefix,
			TableRow tableRow, final QuestionAnswer questionAnswer,
			boolean speakButtonShow) {
		final EditText answerText = new EditText(this.getActivity());
		questionAnswer.clearWidgetIds();
		answerText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
		int widgetId = MainActivity.getUniqueWidgetId(this.getActivity());
		answerText.setId(widgetId);
		ImageButton speakButton = new ImageButton(this.getActivity());
		speakButton.setImageResource(R.drawable.mike);
		speakButton.setOnClickListener(new SpeechButtonClickListener(
				(MainActivity) this.getActivity(), questionAnswer, answerText
						.getId()));
		if (questionAnswer.getInputType() == InputType.INTEGER) {
			answerText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
		} else if (questionAnswer.getInputType() == InputType.EMAIL) {
			answerText
					.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		} else if (questionAnswer.getInputType() == InputType.PHONE) {
			answerText.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
		}
		answerText.addTextChangedListener(new TextBoxListener(questionAnswer,
				prefix));
		TextView label = new TextView(this.getActivity());
		label.setText(prefix.trim());
		viewGroup.addView(label);
		viewGroup.addView(answerText);
		if (questionAnswer.hasChoiceList()) {
			questionAnswer.addWidgetId(viewGroup.getId());
		} else {
			questionAnswer.addWidgetId(answerText.getId());
		}
		viewGroup.addView(speakButton);
		setEditTextWidth(questionAnswer, answerText);
	}

	private void setEditTextWidth(QuestionAnswer questionAnswer,
			EditText editText) {
		int width = 500;
		if (questionAnswer.getAnswerWidth() > 0) {
			width = questionAnswer.getAnswerWidth();
		} else if (questionAnswer.getInputType() == InputType.INTEGER) {
			width = 120;
		}
		editText.setWidth(width);
	}

	private class TextBoxListener implements TextWatcher {
		private final QuestionAnswer questionAnswer;
		private final String prefix;

		private TextBoxListener(QuestionAnswer questionAnswer, String prefix) {
			this.questionAnswer = questionAnswer;
			this.prefix = prefix;
		}

		public void afterTextChanged(Editable editable) {
			String answer = editable.toString();
			if (questionAnswer.hasChoiceList()) {
				answer = PhysicalTherapyUtils.replaceByLabel(
						questionAnswer.getAnswer(), prefix, answer);
			}
			questionAnswer.setAnswer(answer);
		}

		public void beforeTextChanged(CharSequence editable, int start,
				int count, int after) {
		}

		public void onTextChanged(CharSequence editable, int start, int count,
				int after) {
		}
	}
}