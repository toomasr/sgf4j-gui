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
}
