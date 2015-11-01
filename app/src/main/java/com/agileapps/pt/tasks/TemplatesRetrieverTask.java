package com.agileapps.pt.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.R;
import com.agileapps.pt.TemplateDownloaderActivity;
import com.agileapps.pt.manager.ConfigManager;
import com.agileapps.pt.pojos.Config;
import com.agileapps.pt.pojos.StatusAndResponse;
import com.agileapps.pt.pojos.TemplateResponse;
import com.agileapps.pt.util.HttpUtils;
import com.google.gson.Gson;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class TemplatesRetrieverTask extends
		AsyncTask<Object, Integer, StatusAndResponse> {
	private Spinner templateSelector;
	private TextView statusView;

	@Override
	protected StatusAndResponse doInBackground(Object... params) {
		String result = "None";
		templateSelector = (Spinner) params[0];
		statusView = (TextView) params[1];
		try {
			Config config = ConfigManager.getConfig();
			StringBuilder sb = new StringBuilder();
			sb.append(config.getTemplateURL()).append("/")
					.append("templates.php").append("?company=")
					.append(config.getCompany());
			StatusAndResponse statusAndResponse = HttpUtils.getHttpResponse(sb
					.toString());
			Log.i(MainActivity.PT_APP_INFO,"Retreived templates "+statusAndResponse);
			return statusAndResponse;
		} catch (Exception ex) {
			String errorStr = "Unable to get templates because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
			return new StatusAndResponse(-1, errorStr);
		}
	}

	@Override
	protected void onPostExecute(StatusAndResponse statusAndResponse) {
		Gson gson = new Gson();
		if ( statusAndResponse == null ){
			statusView.setText("No status returned");
			return;
		}
		if ( (statusAndResponse.statusCode/100) == 2)
		{
		TemplateResponse templateResponse = gson.fromJson(
				statusAndResponse.message, TemplateResponse.class);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				templateSelector.getContext(),
				R.layout.spinner_item,
				templateResponse.getTemplates());
		templateSelector.setAdapter(adapter);
		}else if ( statusAndResponse.statusCode == -1){
			 statusView.setText( statusAndResponse.message);
		}else{
			 statusView.setText("Http connnectivity issues returned status of "+statusAndResponse.statusCode);
		}
	}

}
