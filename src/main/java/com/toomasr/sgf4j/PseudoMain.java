package com.toomasr.sgf4j;

/*
 * This is a workaround explained at
 * https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle/52571719#52571719
 * 
 * Short story is that:
 * 
 * This error comes from sun.launcher.LauncherHelper in the java.base module.
 * The reason for this is that the Main app extends Application and has a main
 * method. If that is the case, the LauncherHelper will check for the
 * javafx.graphics module to be present as a named module. If that module is
 * not present, the launch is aborted.
 * 
 * The current workaround is to have a PseudoMain class which does not extend
 * the Application and the check will actually pass.
 */
public class PseudoMain {
  public static void main(String[] args) {
    SGF4JApp.main(args);
  }
}
