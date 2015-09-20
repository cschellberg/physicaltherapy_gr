package com.agileapps.pt;

import java.util.Locale;

import com.agileapps.pt.pojos.InputType;
import com.agileapps.pt.pojos.QuestionAnswer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class SpeechButtonClickListener implements OnClickListener {

	public static final String WIDGET_ID_KEY="widgetIdKey";
	public static final String INPUT_TYPE_KEY="inputTypeKey";
	private final QuestionAnswer questionAnswer;
	private final int answerWidgetId;
	private MainActivity activity;

	public SpeechButtonClickListener(MainActivity activity,
			QuestionAnswer questionAnswer,int answerWidgetId) {
		this.questionAnswer = questionAnswer;
		this.answerWidgetId=answerWidgetId;
		this.activity = activity;
	}

	public void onClick(View v) {
		this.promptSpeechInput(v);

	}

	private void promptSpeechInput(View view) {
		try {
		Intent intent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
				Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");
		this.activity.answerWidgetId=answerWidgetId;
		this.activity.answerWidgetDataType=questionAnswer.getInputType();
			activity.startActivityForResult(intent,
					MainActivity.REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(view.getContext(), "Speech Not Supported",
					Toast.LENGTH_SHORT).show();
		}catch (Throwable ex){
			Log.e(MainActivity.PT_APP_INFO,"Cannot process speech because "+ex,ex);
		}
	}
}


