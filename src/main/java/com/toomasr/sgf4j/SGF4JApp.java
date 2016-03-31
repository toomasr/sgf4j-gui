package com.toomasr.sgf4j;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.toomasr.sgf4j.gui.MainUI;
import com.toomasr.sgf4j.gui.Sgf4jGuiUtil;
import com.toomasr.sgf4j.properties.AppState;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SGF4JApp extends Application {
  private static final Logger logger = Logger.getLogger(SGF4JApp.class);

  public SGF4JApp() {
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    AppState.getInstance().loadState();

    primaryStage.setTitle("SGF4J");
    primaryStage.setMinWidth(1200);
    primaryStage.setMinHeight(750);

    MainUI visualBoard = new MainUI();
    Pane topHBox = visualBoard.buildUI();
    visualBoard.initGame();

    Scene scene = new Scene(topHBox, 630, 750);
    scene.getStylesheets().add("/styles.css");

    enableFileDragging(scene);

    primaryStage.setScene(scene);
    primaryStage.show();
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
      buff.append("'"+args[i]+"'");
    }

    if (args.length > 0) {
      logger.debug("Params to the program are"+buff);
    }

    launch(args);
  }

  private static void initializeLogging() {
    BasicConfigurator.configure();

    Layout layout = new SimpleLayout();
    Appender appender;
    try {
      appender = new FileAppender(layout, Sgf4jGuiUtil.getLogFilename());
      logger.addAppender(appender);
    }
    catch (IOException e) {
      System.out.println("WARNING: Unable to init logging properly.");
      e.printStackTrace();

      // falling back to a console appender
      appender = new ConsoleAppender(layout);
      logger.addAppender(appender);
    }
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
//          String filePath = null;
//          for (File file : db.getFiles()) {
//            // initializeGame(Paths.get(file.getAbsolutePath()));
//          }
        }
        event.setDropCompleted(success);
        event.consume();
      }
    });
  }
}
