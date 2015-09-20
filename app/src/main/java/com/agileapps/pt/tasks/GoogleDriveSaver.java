package com.agileapps.pt.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.pojos.FormTemplate;
import com.agileapps.pt.util.PhysicalTherapyUtils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class GoogleDriveSaver extends AsyncTask<Object, Integer, Long> {
	
			@Override
			protected Long doInBackground(Object... params) {
	    		{
	    			try
	    			{
	    			Activity activity=(Activity)params[0];
	  		      // Connect to Google Drive
	                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(DriveScopes.DRIVE));
	    	        Account accounts[]=credential.getAllAccounts();
	    	        if ( accounts.length == 0){
	    	        	return -1l;
	    	        }
		    	     credential.setSelectedAccountName(accounts[0].name);  //TODO match the config settings object
	    	        Drive driveService=getDriveService(credential);
	    	        com.google.api.services.drive.Drive.Files f1 = driveService.files();
					com.google.api.services.drive.Drive.Files.List request = null;
					
					do 
					{
						try 
						{ 
							request = f1.list();
							request.setQ("trashed=false");
							com.google.api.services.drive.model.FileList fileList = request.execute();
							
							for (File file:fileList.getItems()){
								Log.i(MainActivity.PT_APP_INFO,"file is "+file.getDownloadUrl());
							}
							request.setPageToken(fileList.getNextPageToken());
						} catch (UserRecoverableAuthIOException e) {
							//TODOstartActivityForResult(e.getIntent(), MainActivity.REQUEST_AUTHORIZATION);
						} catch (IOException e) {
							e.printStackTrace();
							if (request != null)
							{
								request.setPageToken(null);
							}
						}
					} while (request.getPageToken() !=null && request.getPageToken().length() > 0);
	    			}catch(Throwable ex){
	    				Log.e(MainActivity.PT_APP_INFO,"Unable to save to google drive because "+ex);
	    				return -1l;
	    			}
					return 9l;
	    		}

			}
			
			private Drive getDriveService(HttpRequestInitializer credential) {
				return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
						new GsonFactory(), credential).build();
			}
}
