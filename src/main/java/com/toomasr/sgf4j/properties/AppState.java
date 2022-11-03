package com.toomasr.sgf4j.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.SGF4JApp;
import com.toomasr.sgf4j.gui.Sgf4jGuiUtil;

public class AppState {
	private static final Logger logger = LoggerFactory.getLogger(SGF4JApp.class);
  public static final String PROPERTIES_FILE_NAME = "sgf4j-gui.properties";
  public static final String CURRENT_FILE = "current-file";
  
  private static final AppState INSTANCE = new AppState();
  
  private final Properties PROPERTIES = new Properties();
  private final File propertiesFile;

  private AppState() {
    propertiesFile = new File(Sgf4jGuiUtil.getAppHomeFolder(), PROPERTIES_FILE_NAME);
  }
  
  public static AppState getInstance() {
    return INSTANCE;
  }

  public void saveState() {
    try {
      PROPERTIES.store(new FileOutputStream(propertiesFile), "");
    }
    catch (FileNotFoundException e) {
      logger.info("Unable to find "+PROPERTIES_FILE_NAME+ " Not loading properties.");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void loadState() throws FileNotFoundException {
    try {
      PROPERTIES.load(new FileInputStream(propertiesFile));
    }
    catch (FileNotFoundException e) {
      throw e;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void addProperty(String key, String value) {
    PROPERTIES.put(key, value);
  }

  public String getProperty(String key) {
    return PROPERTIES.getProperty(key, null);
  }
}
