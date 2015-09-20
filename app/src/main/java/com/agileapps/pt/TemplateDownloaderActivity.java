package com.agileapps.pt;

import java.util.ArrayList;
import java.util.List;

import com.agileapps.pt.manager.ConfigManager;
import com.agileapps.pt.pojos.Config;
import com.agileapps.pt.tasks.TemplateDownloadTask;
import com.agileapps.pt.tasks.TemplatesRetrieverTask;

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
import android.widget.TextView;
import android.widget.Toast;

public class TemplateDownloaderActivity extends Activity {
	
	public static final int GOT_TEMPLATES=1;

	public static final int ERROR_RETURNED_GETTING_TEMPLATES = 2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_templates);
		Config config = null;
		try {
			config = ConfigManager.getConfig();
		} catch (Exception ex) {
			String errorStr = "Cannot get configuration because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
			return;
		}
		final Spinner templateSelector = (Spinner) findViewById(R.id.templateSelector);
		TextView statusView=(TextView)findViewById(R.id.downloadTemplateStatus);
        (new TemplatesRetrieverTask()).execute( templateSelector,statusView);
		Button downloadTemplateButton = (Button) this.findViewById(R.id.downloadTemplate);
		downloadTemplateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String templateFileName = (String) templateSelector.getSelectedItem();
				(new TemplateDownloadTask()).execute( templateFileName);
				Intent intent = new Intent(TemplateDownloaderActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		Button cancelTemplateButton = (Button) this
				.findViewById(R.id.cancelTemplate);
		cancelTemplateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(TemplateDownloaderActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	public void onBackPressed() {
		Intent mainIntent= new Intent(TemplateDownloaderActivity.this,MainActivity.class);
		startActivity(mainIntent);
		finish();
	}


}
