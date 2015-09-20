package com.agileapps.pt;

import java.util.List;

import com.agileapps.pt.manager.ConfigManager;
import com.agileapps.pt.manager.FormTemplateManager;
import com.agileapps.pt.pojos.Config;
import com.agileapps.pt.util.PhysicalTherapyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ChangeTemplateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_template);
		final Spinner templateSelector = (Spinner) findViewById(R.id.templateSelector);
		try {
			Config config = ConfigManager.getConfig();
			List<String> templateList = FormTemplateManager
					.getFormTemplateNames();
			String[] items = new String[templateList.size()];
			templateList.toArray(items);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item, items);
			templateSelector.setAdapter(adapter);
			templateSelector.setSelection(PhysicalTherapyUtils
					.getSelectedIndex(config.getDefaultFormTemplate(), items));
		} catch (Exception ex) {
			String errorStr = "Cannot get template names because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
			return;
		}

		Button changeTemplateButton = (Button) this
				.findViewById(R.id.changeTemplateButton);
		changeTemplateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				FormTemplateManager.temporaryFormTemplateOverride = (String) templateSelector
						.getSelectedItem();
				Intent intent = new Intent(ChangeTemplateActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

		Button deleteTemplateButton = (Button) this
				.findViewById(R.id.deleteTemplateButton);
		deleteTemplateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				 FormTemplateManager.deleteTemplate( (String) templateSelector
						.getSelectedItem());
				Intent intent = new Intent(ChangeTemplateActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

		Button cancelChangeTemplateButton = (Button) this
				.findViewById(R.id.cancelChangeTemplateButton);
		cancelChangeTemplateButton
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						Intent intent = new Intent(ChangeTemplateActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();
					}
				});

	}

	@Override
	public void onBackPressed() {
		Intent mainIntent = new Intent(ChangeTemplateActivity.this,
				MainActivity.class);
		startActivity(mainIntent);
		finish();
	}

}
