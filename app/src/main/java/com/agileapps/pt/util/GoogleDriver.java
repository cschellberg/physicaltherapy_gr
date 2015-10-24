package com.agileapps.pt.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.exceptions.GoogleDriverException;
import com.agileapps.pt.manager.FormTemplateManager;
import com.agileapps.pt.pojos.FormTemplate;
import com.agileapps.pt.pojos.GoogleFileType;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;

import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dschellb on 9/26/2015.
 */
public class GoogleDriver {

    private final static String STORAGE_DIR = "pt_storage";
    private final static String REPORT_DIR = "pt_report";
    private static final long SYNC_INTERVAL = 120000l ;
    private DriveId storageDriveId;
    private DriveId reportDriveId;
    private List<String>formNames=new ArrayList<String>();
    private static GoogleDriver googleDriver;
    private long lastSync=0;

    private GoogleDriver() {
    }

    public static synchronized GoogleDriver getInstance(GoogleApiClient googleClient) throws GoogleDriverException {
        if (googleClient == null) {
            throw new GoogleDriverException("Cannot initialize google driver because googleClient is null");
        }
        if (googleDriver == null) {
            googleDriver = new GoogleDriver();
        }
        if (googleDriver.storageDriveId == null) {
            googleDriver.setStorageDirectory(googleClient);
        }
        if (googleDriver.reportDriveId == null) {
            googleDriver.setReportDirectory(googleClient);
        }
        if ((System.currentTimeMillis()-googleDriver.lastSync) >= SYNC_INTERVAL) {
            Drive.DriveApi.requestSync(googleClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    Log.i(MainActivity.PT_APP_INFO,"Synched with google drive.  Status is "+status);
                }
            });
            googleDriver.lastSync=System.currentTimeMillis();
        }
        return googleDriver;
    }


    private void setStorageDirectory(final GoogleApiClient googleClient) throws GoogleDriverException {
        Query.Builder queryBuilder = new Query.Builder();
        queryBuilder.addFilter(Filters.eq(SearchableField.TITLE, STORAGE_DIR));
        Query query = queryBuilder.build();
        try {
            final DriveFolder driveFolder = Drive.DriveApi.getRootFolder(googleClient);
             driveFolder.queryChildren(googleClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult resultBufferResult) {
                    for (Metadata result : resultBufferResult.getMetadataBuffer()) {
                        if (result.getTitle().equals(STORAGE_DIR)) {
                            storageDriveId = result.getDriveId();
                        }
                    }
                    if (storageDriveId == null) {
                        MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                        builder.setTitle(STORAGE_DIR);
                        driveFolder.createFolder(googleClient, builder.build()).setResultCallback(
                                new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                                        storageDriveId = driveFolderResult.getDriveFolder().getDriveId();
                                    }
                                }
                        );

                    }
                }
            });
        } catch (Throwable th) {
            throw new GoogleDriverException("Unable to find or create google drive storage directory because " + th, th);
        }
    }


    private void setReportDirectory(final GoogleApiClient googleClient) throws GoogleDriverException {
        Query.Builder queryBuilder = new Query.Builder();
        queryBuilder.addFilter(Filters.eq(SearchableField.TITLE, REPORT_DIR));
        Query query = queryBuilder.build();
        try {
            final DriveFolder driveFolder = Drive.DriveApi.getRootFolder(googleClient);
            driveFolder.queryChildren(googleClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult resultBufferResult) {
                    for (Metadata result : resultBufferResult.getMetadataBuffer()) {
                        if (result.getTitle().equals(REPORT_DIR)) {
                            reportDriveId = result.getDriveId();
                        }
                    }
                    if (reportDriveId == null) {
                        MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                        builder.setTitle(REPORT_DIR);
                        driveFolder.createFolder(googleClient, builder.build()).setResultCallback(
                                new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                                        reportDriveId = driveFolderResult.getDriveFolder().getDriveId();
                                    }
                                }
                        );
                    }
                }
            });
        } catch (Throwable th) {
            throw new GoogleDriverException("Unable to find or create google drive report directory because " + th, th);
        }

    }

    private void save(final FormTemplate formToSave, final GoogleApiClient googleClient, final DriveId dirDriveId,
                      final String fileName) {
        PendingResult<DriveApi.DriveContentsResult> driveContentsResults = Drive.DriveApi.newDriveContents(googleClient);
        driveContentsResults.setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            public void onResult(DriveApi.DriveContentsResult result) {
                DriveFolder ptFolder = Drive.DriveApi.getFolder(googleClient, dirDriveId);
                DriveContents driveContents = result.getDriveContents();
                Persister persister = new Persister();
                try {
                    persister.write(formToSave, driveContents.getOutputStream());
                } catch (Exception ex) {
                    Log.e(MainActivity.PT_APP_INFO, "Cannot save for to google drive because " + ex);
                }
                MetadataChangeSet.Builder builder = new MetadataChangeSet.Builder();
                builder.setTitle(fileName);
                ptFolder.createFile(googleClient, builder.build(), driveContents);
            }
        });
    }

    public void update(final FormTemplate formToSave, final GoogleApiClient googleClient,
                       final DriveId fileDriveId, final String fileName) {
        DriveFile driveFile = Drive.DriveApi.getFile(googleClient, fileDriveId);
        driveFile.open(googleClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            public void onResult(DriveApi.DriveContentsResult result) {
                Persister persister = new Persister();
                try {
                    persister.write(formToSave, result.getDriveContents().getOutputStream());
                } catch (Exception ex) {
                    Log.e(MainActivity.PT_APP_INFO, "Cannot save for to google drive because " + ex);
                }
            }
        });
    }

    public  List<String> getAllForms(GoogleApiClient googleClient){
        final List<String>tmpFormNames=new ArrayList<String>();
        final DriveFolder driveFolder = Drive.DriveApi.getFolder(googleClient, storageDriveId);
        driveFolder.listChildren(googleClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult resultBufferResult) {
                for (Metadata result : resultBufferResult.getMetadataBuffer()) {
                   tmpFormNames.add(result.getTitle());
                 }
                formNames=tmpFormNames;
            }});
        return formNames;
    }

    public void saveOrUpdate(final FormTemplate formToSave, final GoogleApiClient googleClient,
                             final GoogleFileType googleFileType) throws GoogleDriverException {
        if (storageDriveId == null) {
            throw new GoogleDriverException("Cannot found form directory on google drive");
        }
        final DriveId dirDriveId;
        if (googleFileType == GoogleFileType.REPORT) {
            dirDriveId = reportDriveId;
        } else {
            dirDriveId = storageDriveId;
        }
        final String fileName = PhysicalTherapyUtils.getFileName(formToSave);
        Query.Builder queryBuilder = new Query.Builder();
        queryBuilder.addFilter(Filters.eq(SearchableField.TITLE, fileName));
        Query query = queryBuilder.build();
        try {
            final DriveFolder driveFolder = Drive.DriveApi.getFolder(googleClient, dirDriveId);
            driveFolder.queryChildren(googleClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult resultBufferResult) {
                    DriveId fileDriveId = null;
                    for (Metadata result : resultBufferResult.getMetadataBuffer()) {
                        fileDriveId = result.getDriveId();
                    }
                    if (fileDriveId == null) {
                        save(formToSave, googleClient, dirDriveId, fileName);
                    } else {
                        update(formToSave, googleClient, fileDriveId, fileName);
                    }
                }
            });
        } catch (Throwable th) {
            throw new GoogleDriverException("Unable to find or save file to drive because " + th, th);
        }

    }

    public void loadForm(final GoogleApiClient googleClient,final Context context,final String formName){
        Query.Builder queryBuilder = new Query.Builder();
        Log.i(MainActivity.PT_APP_INFO,"Load form from cloud.  Google client state "+googleClient.isConnected());
        queryBuilder.addFilter(Filters.eq(SearchableField.TITLE, formName));
        Query query = queryBuilder.build();
             final DriveFolder driveFolder = Drive.DriveApi.getFolder(googleClient, storageDriveId);
            driveFolder.queryChildren(googleClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult resultBufferResult) {
                    if (resultBufferResult.getMetadataBuffer().getCount() > 0) {
                        DriveFile driveFile=Drive.DriveApi.getFile(googleClient, resultBufferResult.getMetadataBuffer().get(0).getDriveId());
                        driveFile.open(googleClient,DriveFile.MODE_READ_ONLY,null)
                                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                        try {
                                            InputStream is = driveContentsResult.getDriveContents().getInputStream();
                                            FormTemplateManager.loadForm(is);
                                            Intent intent = new Intent(context, MainActivity.class);
                                            context.startActivity(intent);
                                        }catch(Throwable th){
                                            String errorStr="Unable to retrieve form "+formName+ " because "+th;
                                            Log.e(MainActivity.PT_APP_INFO,errorStr);
                                            Toast.makeText(context, errorStr,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                    }
                }});
    }

}
