package com.toomasr.sgf4j;


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
    primaryStage.setTitle("SGF4J");
    primaryStage.setMinWidth(1125);
    primaryStage.setMinHeight(800);
    primaryStage.getIcons().add(new Image(SGF4JApp.class.getResourceAsStream("/icon.png")));

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
        }
        else {
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
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    mainUIBuilder.initGame();
    this.scene.setRoot(mainUI);

  }

  @Override
  public void init() throws Exception {
    super.init();
    System.out.println(Font.getDefault());
    Font.loadFont(SGF4JApp.class.getResource("/fonts/Poly-Regular.otf").toExternalForm(), 10);
    Font.loadFont(SGF4JApp.class.getResource("/fonts/arvo/Arvo-Regular.ttf").toExternalForm(), 10);
    Font font = Font.loadFont(SGF4JApp.class.getResource("/fonts/open-sans/OpenSans-Regular.ttf").toExternalForm(), 10);
    System.out.println(font);
    font = Font.loadFont(SGF4JApp.class.getResource("/fonts/open-sans/OpenSans-Bold.ttf").toExternalForm(), 10);
    System.out.println(font);
  }
}
