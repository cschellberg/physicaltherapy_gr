package com.agileapps.pt;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class EntryActivity extends Activity {

	private static final int REQ_CODE_SPEECH_INPUT = 1;

	private int originatingWidget;

	private String dataType;
	
	//TODO this has to be changed in the future to assure r.ids are not duplicate
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		originatingWidget = extras.getInt(MainActivity.HOME_WIDGET);
		dataType = extras.getString(MainActivity.HOME_WIDGET_TYPE);
		setContentView(R.layout.integer_voice_entry);
		ImageButton speakButton = (ImageButton) this
				.findViewById(R.id.btnSpeak);
		speakButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				promptSpeechInput();
			}
		});

		Button buttonSave = (Button) findViewById(R.id.btnSave);
		buttonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EntryActivity.this.goBackToMain();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.btnCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EntryActivity.this.goBackToMain();
			}
		});
	}

	private void goBackToMain() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.HOME_WIDGET, this.originatingWidget);
		EditText numberInput = (EditText) this.findViewById(R.id.someNumber);
		intent.putExtra(MainActivity.HOME_WIDGET_VALUE, numberInput.getText()
				.toString());
		startActivity(intent);
	}

	private void promptSpeechInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(this, "Speech Not Supported", Toast.LENGTH_SHORT)
					.show();

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQ_CODE_SPEECH_INPUT: {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> result = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String inputStr = result.get(0).trim();
				if (MainActivity.HOME_WIDGET_TYPE_INTEGER.equals(dataType)) {
					inputStr = "Not a number.  Please reenter";
					for (String aResult : result) {
						try {
							aResult = aResult.trim();
							Integer.parseInt(aResult);
							inputStr = aResult;
						} catch (Exception ex) {
							// ignore
						}
					}
				}
				EditText numberInput = (EditText) this
						.findViewById(R.id.someNumber);
				numberInput.setText(inputStr);
			}
			break;
		}

		}
	}
}
