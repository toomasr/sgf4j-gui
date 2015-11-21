package com.toomasr.sgf4j.gui;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Sgf4jGuiUtil {
  public static List<File> getRootDevices() {
    File[] roots = File.listRoots();
    return Arrays.asList(roots);
  }

  public static File getHomeFolder() {
    return new File(System.getProperty("user.home"));
  }
  
  public static File getAppHomeFolder() {
    File appHomeFolder = new File( getHomeFolder(), ".sgf4j-gui");
    if (!appHomeFolder.exists()) {
      boolean flag = appHomeFolder.mkdir();
      if (!flag) {
        throw new RuntimeException("Unable to create app home folder "+appHomeFolder.getAbsolutePath());
      }
    }
    return appHomeFolder;
  }
}
