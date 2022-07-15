package com.toomasr.sgf4j;

import java.awt.Taskbar;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.gui.MainUI;
import com.toomasr.sgf4j.properties.AppState;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import de.codecentric.centerdevice.javafxsvg.dimension.PrimitiveDimensionProvider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SGF4JApp extends Application {
  private static final Logger logger = LoggerFactory.getLogger(SGF4JApp.class);
  private Scene scene;

  public SGF4JApp() {
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    AppState.getInstance().loadState();

    SvgImageLoaderFactory.install(new PrimitiveDimensionProvider());
    String verInfo = extractVersionFromManifest();
    primaryStage.setTitle("SGF4J - " + verInfo);
    System.out.println("Build information: " + verInfo);

    String javaVersion = System.getProperty("java.version");
    System.out.println("Java: " + javaVersion);

    String javaFxVersion = System.getProperty("javafx.runtime.version");
    System.out.println("JavaFX: " + javaFxVersion);

    primaryStage.setMinWidth(1125);
    primaryStage.setMinHeight(800);
    primaryStage.getIcons().add(new Image(SGF4JApp.class.getResourceAsStream("/icon.png")));

    // set Dock icon
    // https://stackoverflow.com/questions/24159825/changing-application-dock-icon-javafx-programmatically
    try {
      URL iconURL = SGF4JApp.class.getResource("/icon.png");
      java.awt.Image image = new ImageIcon(iconURL).getImage();

      Taskbar.getTaskbar().setIconImage(image);
    } catch (Exception e) {
      // Won't work on Windows or Linux.
    }
    // end of Dock icon

    MainUI mainUIBuilder = new MainUI(this);
    Pane mainUI = mainUIBuilder.buildUI();
    mainUIBuilder.initGame();

    this.scene = new Scene(mainUI);
    scene.getStylesheets().add("/styles.css");

    enableFileDragging(scene);

    primaryStage.setScene(scene);
    primaryStage.show();

    mainUIBuilder.fireUiVisibleEvent();
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    AppState.getInstance().saveState();
  }

  public static void main(String[] args) {
    initializeLogging();

    StringBuffer buff = new StringBuffer();
    for (int i = 0; i < args.length; i++) {
      buff.append("'" + args[i] + "'");
    }

    if (args.length > 0) {
      logger.debug("Params to the program are" + buff);
    }

    launch(args);
  }

  private static void initializeLogging() {
    // Layout layout = new SimpleLayout();
    // Appender appender;
    // try {
    // appender = new FileAppender(layout, Sgf4jGuiUtil.getLogFilename());
    // logger.addAppender(appender);
    // }
    // catch (IOException e) {
    // System.out.println("WARNING: Unable to init logging properly.");
    // e.printStackTrace();
    // }
  }

  private void enableFileDragging(Scene scene) {
    scene.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        } else {
          event.consume();
        }
      }
    });

    scene.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
          success = true;
          // String filePath = null;
          // for (File file : db.getFiles()) {
          // // initializeGame(Paths.get(file.getAbsolutePath()));
          // }
        }
        event.setDropCompleted(success);
        event.consume();
      }
    });
  }

  public void scheduleRestartUI() {
    Platform.runLater(() -> {
      restartUI();
    });
  }

  private void restartUI() {
    MainUI mainUIBuilder = new MainUI(this);
    Pane mainUI;
    try {
      mainUI = mainUIBuilder.buildUI();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    mainUIBuilder.initGame();
    this.scene.setRoot(mainUI);
  }

  @Override
  public void init() throws Exception {
    super.init();
    Font.loadFont(SGF4JApp.class.getResource("/fonts/open-sans/OpenSans-Regular.ttf").toExternalForm(), 10);
    Font.loadFont(SGF4JApp.class.getResource("/fonts/open-sans/OpenSans-Bold.ttf").toExternalForm(), 10);
  }

  private String extractVersionFromManifest() {
    String rtrn = "";
    try {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        try {
          Manifest manifest = new Manifest(resources.nextElement().openStream());
          // check that this is your manifest and do what you need or get the next one
          Map<String, Attributes> entries = manifest.getEntries();
          final String key = "SGF4J Build Information";
          if (entries.size() > 0 && key.equals(entries.keySet().iterator().next())) {
            String implVersion = entries.get(key).getValue("Implementation-Version");
            String implRevision = entries.get(key).getValue("Implementation-SCM-Revision");
            String implBranch = entries.get(key).getValue("Implementation-SCM-Branch");
            String implBuildtime = entries.get(key).getValue("Build-Time");
            if (!"null".equals(implVersion)) {
              rtrn += implVersion + "; ";
            }
            if (!"null".equals(implRevision)) {
              rtrn += implRevision + "; ";
            }
            if (!"null".equals(implBranch)) {
              rtrn += implBranch + "; ";
            }
            if (!"null".equals(implBuildtime)) {
              rtrn += implBuildtime + "; ";
            }
          }

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if ("".equals(rtrn)) {
      rtrn = "No build info available";
    }
    return rtrn;
  }
}
