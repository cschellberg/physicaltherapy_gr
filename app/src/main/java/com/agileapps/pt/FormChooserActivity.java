package com.agileapps.pt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.agileapps.pt.manager.FormTemplateManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class FormChooserActivity extends Activity {

	private static final String NONE_SELECTED = "None selected";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_chooser);
		populateClientSelectBox();
		Button deleteButton = (Button) this.findViewById(R.id.deleteForm);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Spinner clientSelector = (Spinner) findViewById(R.id.clientSelector);
				Spinner formSelector = (Spinner) findViewById(R.id.formSelector);
				Spinner dateSelector = (Spinner) findViewById(R.id.dateSelector);
				String clientString = (String) clientSelector.getSelectedItem();
				String formString = (String) formSelector.getSelectedItem();
				String dateString = (String) dateSelector.getSelectedItem();
				if (clientString.equals(NONE_SELECTED)
						|| formString.equals(NONE_SELECTED)
						|| dateString.equals(NONE_SELECTED)) {
					return;
				}
				File filesDir = new File(Environment
						.getExternalStorageDirectory(), MainActivity.FORM_DIR);
				String filePath = clientString + "/" + formString + "/"
						+ dateString;
				File datesDir = new File(filesDir, filePath);
				for ( File file:datesDir.listFiles()){
					file.delete();
				}
				datesDir.delete();
				Toast.makeText(FormChooserActivity.this,dateString+" directory deleted ", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(FormChooserActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		Button cancelButton = (Button) this.findViewById(R.id.cancelForm);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent mainIntent= new Intent(FormChooserActivity.this,MainActivity.class);
				startActivity(mainIntent);
				finish();
			}
		});
				
		Button viewButton = (Button) this.findViewById(R.id.viewForm);
		viewButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Spinner clientSelector = (Spinner) findViewById(R.id.clientSelector);
				Spinner formSelector = (Spinner) findViewById(R.id.formSelector);
				Spinner dateSelector = (Spinner) findViewById(R.id.dateSelector);
				Spinner fileSelector = (Spinner) findViewById(R.id.fileSelector);
				String clientString = (String) clientSelector.getSelectedItem();
				String formString = (String) formSelector.getSelectedItem();
				String dateString = (String) dateSelector.getSelectedItem();
				String fileString = (String) fileSelector.getSelectedItem();
				if (clientString.equals(NONE_SELECTED)
						|| formString.equals(NONE_SELECTED)
						|| dateString.equals(NONE_SELECTED)
						|| fileString.equals(NONE_SELECTED)) {
					return;
				}
				File filesDir = new File(Environment
						.getExternalStorageDirectory(), MainActivity.FORM_DIR);
				String filePath = clientString + "/" + formString + "/"
						+ dateString+"/"+fileString;
				File formFile = new File(filesDir, filePath);
				try {
					FormTemplateManager.loadForm(formFile);
					Intent intent = new Intent(FormChooserActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				} catch (Exception ex) {
					String errorStr="Unable to retrieve form because "+ex;
					Toast.makeText(FormChooserActivity.this,errorStr, Toast.LENGTH_LONG).show();
					Log.e(MainActivity.PT_APP_INFO,errorStr);
				} 
			}
		});
	}

	@Override
	public void onBackPressed() {
		Intent mainIntent= new Intent(FormChooserActivity.this,MainActivity.class);
		startActivity(mainIntent);
		finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void populateClientSelectBox() {
		final Spinner clientSelector = (Spinner) findViewById(R.id.clientSelector);
		final Spinner formSelector = (Spinner) findViewById(R.id.formSelector);
		final Spinner dateSelector = (Spinner) findViewById(R.id.dateSelector);
		final Spinner fileSelector = (Spinner) findViewById(R.id.fileSelector);
		File filesDir = new File(Environment.getExternalStorageDirectory(),
				MainActivity.FORM_DIR);
		List<String> clientList = new ArrayList<String>();
		clientList.add(NONE_SELECTED);
		for (File file : filesDir.listFiles()) {
			clientList.add(file.getName());
		}
		String[] items = new String[clientList.size()];
		clientList.toArray(items);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, items);

		clientSelector.setAdapter(adapter);
		clientSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int postion, long id) {
				String selectedItemString = (String) adapterView
						.getSelectedItem();
				if (selectedItemString.equals(NONE_SELECTED)) {
					return;
				}
				File filesDir = new File(Environment
						.getExternalStorageDirectory(), MainActivity.FORM_DIR);
				File formsDir = new File(filesDir, selectedItemString);
				List<String> formList = new ArrayList<String>();
				formList.add(NONE_SELECTED);
				for (File formDir : formsDir.listFiles()) {
					formList.add(formDir.getName());
				}
				String[] items = new String[formList.size()];
				formList.toArray(items);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						FormChooserActivity.this,
						R.layout.spinner_item, items);
				formSelector.setAdapter(adapter);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		formSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> viewAdapter, View view,
					int position, long id) {
				String clientString = (String) clientSelector.getSelectedItem();
				String formString = (String) formSelector.getSelectedItem();
				if (formString.equals(NONE_SELECTED)) {
					return;
				}
				File filesDir = new File(Environment
						.getExternalStorageDirectory(), MainActivity.FORM_DIR);
				File formsDir = new File(filesDir, clientString + "/"
						+ formString);
				List<String> dateList = new ArrayList<String>();
				dateList.add(NONE_SELECTED);
				for (File formDir : formsDir.listFiles()) {
					dateList.add(formDir.getName());
				}
				String[] items = new String[dateList.size()];
				dateList.toArray(items);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						FormChooserActivity.this,
						R.layout.spinner_item, items);
				dateSelector.setAdapter(adapter);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		dateSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> viewAdapter, View view,
					int position, long id) {
				String clientString = (String) clientSelector.getSelectedItem();
				String formString = (String) formSelector.getSelectedItem();
				String dateString = (String) dateSelector.getSelectedItem();
				if (dateString.equals(NONE_SELECTED)) {
					return;
				}
				File filesDir = new File(Environment
						.getExternalStorageDirectory(), MainActivity.FORM_DIR);
				String filePath = clientString + "/" + formString + "/"
						+ dateString;
				File datesDir = new File(filesDir, filePath);
				List<String> fileList = new ArrayList<String>();
				fileList.add(NONE_SELECTED);
				for (File fileDir : datesDir.listFiles()) {
					fileList.add(fileDir.getName());
				}
				String[] items = new String[fileList.size()];
				fileList.toArray(items);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						FormChooserActivity.this,
						R.layout.spinner_item, items);
				fileSelector.setAdapter(adapter);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		fileSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

	}

}
