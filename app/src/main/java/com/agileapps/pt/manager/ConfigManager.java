package com.agileapps.pt.manager;

import java.io.File;

import org.simpleframework.xml.core.Persister;

import android.os.Environment;

import com.agileapps.pt.pojos.Config;

public class ConfigManager {

	private final static String CONFIG_FILE = "config.xml";

	private static Config config;

	public synchronized static Config getConfig() throws Exception {
		if (config == null) {
			initializeConfig();
		}
		return config;
	}

	private static void initializeConfig() throws Exception {
		File configFile = new File(Environment.getExternalStorageDirectory(),
				CONFIG_FILE);
		if (configFile.exists()) {
			Persister persister=new Persister();
			config=persister.read(Config.class, configFile);

		} else {
			config = new Config();
		}
	}

	public static void saveConfig(Config configObj) throws Exception{
		config=configObj;
		File configFile = new File(Environment.getExternalStorageDirectory(),
				CONFIG_FILE);
		Persister persister=new Persister();
        persister.write(config, configFile);
	}
	
}
