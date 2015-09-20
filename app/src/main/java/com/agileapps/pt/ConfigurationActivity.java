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

public class ConfigurationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		Button saveConfigButton = (Button) this.findViewById(R.id.saveConfig);
		Config config = null;
		try {
			config = ConfigManager.getConfig();
		} catch (Exception ex) {
			String errorStr = "Cannot get configuration because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
			return;
		}
		EditText companyEdit = (EditText) this.findViewById(R.id.company);
		companyEdit.setText(config.getCompany());

		try {
			Spinner clientTemplateSelector = (Spinner) findViewById(R.id.clientTemplateSelector);
			List<String> clientTemplateList = FormTemplateManager
					.getClientInfoFormTemplateNames();
			String[] items = new String[clientTemplateList.size()];
	
			clientTemplateList.toArray(items);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item, items);
			clientTemplateSelector.setAdapter(adapter);
			clientTemplateSelector.setSelection(PhysicalTherapyUtils.getSelectedIndex(config.getDefaultClientInfoTemplate(), items));

			Spinner templateSelector = (Spinner) findViewById(R.id.templateSelector);
			List<String> templateList = FormTemplateManager
					.getFormTemplateNames();
			items = new String[templateList.size()];
			templateList.toArray(items);
			adapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item, items);
			templateSelector.setAdapter(adapter);
			templateSelector.setSelection(PhysicalTherapyUtils.getSelectedIndex(config.getDefaultFormTemplate(), items));

		} catch (Exception ex) {
			String errorStr = "Cannot get template name because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
			return;
		}

		EditText templateUrlEdit = (EditText) this
				.findViewById(R.id.templateUrl);
		templateUrlEdit.setText(config.getTemplateURL());
		saveConfigButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ConfigurationActivity.this.saveConfig();
				Intent intent = new Intent(ConfigurationActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		Button cancelConfigButton = (Button) this
				.findViewById(R.id.cancelConfig);
		cancelConfigButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(ConfigurationActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

	}
	
	@Override
	public void onBackPressed() {
		Intent mainIntent= new Intent(ConfigurationActivity.this,MainActivity.class);
		startActivity(mainIntent);
		finish();
	}
	
	protected void saveConfig() {
		try {
			Config config = new Config();
			EditText companyEdit = (EditText) this.findViewById(R.id.company);
			config.setCompany(companyEdit.getText().toString());
			Spinner clientTemplateSelector = (Spinner) findViewById(R.id.clientTemplateSelector);
			config.setDefaultClientInfoTemplate(clientTemplateSelector
					.getSelectedItem().toString());
			Spinner templateSelector = (Spinner) findViewById(R.id.templateSelector);
			config.setDefaultFormTemplate(templateSelector.getSelectedItem()
					.toString());
			EditText templateUrlEdit = (EditText) this
					.findViewById(R.id.templateUrl);
			config.setTemplateURL(templateUrlEdit.getText().toString());
			ConfigManager.saveConfig(config);
		} catch (Exception ex) {
			String errorStr = "Unable to save configuration because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
			return;
		}
	}

}
