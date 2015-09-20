package com.agileapps.pt.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.manager.ConfigManager;
import com.agileapps.pt.manager.FormTemplateManager;
import com.agileapps.pt.pojos.Config;
import com.agileapps.pt.pojos.StatusAndResponse;
import com.agileapps.pt.util.HttpUtils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class TemplateDownloadTask extends AsyncTask<String, Integer, Integer> {

	public static final String TEMPLATES = "templates";

	@Override
	protected Integer doInBackground(String... params) {
		String fileName = params[0];
		FileOutputStream fos = null;
		try {
			Config config = ConfigManager.getConfig();
			StringBuilder sb = new StringBuilder();
			sb.append(config.getTemplateURL()).append("/")
					.append("getTemplate.php").append("?company=")
					.append(config.getCompany()).append("&template=")
					.append(fileName);
			StatusAndResponse statusAndResponse = HttpUtils.getHttpResponse(sb
					.toString());
			if ((statusAndResponse.statusCode / 100) == 2) {
				File templatesDir = new File(
						Environment.getExternalStorageDirectory(), TEMPLATES);
				if (!templatesDir.exists()) {
					templatesDir.mkdir();
				}
				File templateFile = new File(templatesDir, fileName);
				fos = new FileOutputStream(templateFile);
				fos.write(statusAndResponse.message.getBytes());
				fos.flush();
			} else {
				Log.e(MainActivity.PT_APP_INFO,
						"Could not download file.  Server returned a status of code "
								+ statusAndResponse.statusCode);
			}
		} catch (Exception ex) {
			String errorStr = "Unable to download templates because " + ex;
			Log.e(MainActivity.PT_APP_INFO, errorStr);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore
				}
			}
			try {
				FormTemplateManager.initFormTemplateMap();
			} catch (Exception ex) {
				String errorStr = "Unable to reload form template map  because "
						+ ex;
				Log.e(MainActivity.PT_APP_INFO, errorStr);
			}
		}
		return 1;
	}

}
