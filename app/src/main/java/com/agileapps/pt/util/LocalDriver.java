package com.agileapps.pt.util;

import android.os.Environment;
import android.util.Log;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.pojos.FormTemplate;
import com.agileapps.pt.pojos.GoogleFileType;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dschellb on 10/25/2015.
 */
public class LocalDriver {


    public static void save(final FormTemplate formToSave, final GoogleFileType googleFileType, String fileName) {
        File storageDir = new File(Environment
                .getExternalStorageDirectory(), MainActivity.STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        if ( fileName == null ) {
            fileName = PhysicalTherapyUtils.getFileName(formToSave);
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File(storageDir,fileName));
            Persister persister = new Persister();
            persister.write(formToSave, fos);
        } catch (Exception ex) {
            Log.e(MainActivity.PT_APP_INFO, "Unable to save form to local storage because " + ex.getMessage());
        }
    }

    public static List<String> getAllForms() {
        List<String> retList = new ArrayList<String>();
        File storageDir = new File(Environment
                .getExternalStorageDirectory(), MainActivity.STORAGE_DIR);
        if (storageDir.exists()) {
            File formFiles[] = storageDir.listFiles();
            for (File formFile : formFiles) {
                retList.add(formFile.getName());
            }
        }
        return retList;
    }

    public static InputStream getLocalInputStream(String formName)  {
        InputStream is = null;
        File storageDir = new File(Environment
                .getExternalStorageDirectory(), MainActivity.STORAGE_DIR);
        if (storageDir.exists()) {
            File file = new File(storageDir.getPath(), formName);
            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                }catch(Exception ex){
                    Log.e(MainActivity.PT_APP_INFO,"Could not retrieve file on local drive because "+ex);
                }
            }
        }else{
            storageDir.mkdirs();
        }
        return is;
    }

    public static List<String> loadFormNamesFromLocalStorage() {
        List<String>retList=new ArrayList<String>();
        File storageDir = new File(Environment
                .getExternalStorageDirectory(), MainActivity.STORAGE_DIR);
        if (storageDir.exists()){
            File files[]=storageDir.listFiles();
            for (File file:files){
                retList.add(file.getName());
            }
        }else{
            storageDir.mkdirs();
        }
        return retList;
    }

}
