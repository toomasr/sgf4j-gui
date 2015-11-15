package com.toomasr.sgf4j.gui;

import java.io.File;

import com.toomasr.board.MainUI;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SGF4JApp extends Application {
  static {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
  }

  public SGF4JApp() {
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("SGF4J");
    primaryStage.setMinWidth(1200);
    primaryStage.setMinHeight(750);

    MainUI visualBoard = new MainUI();
    Pane topHBox = visualBoard.buildUI();

    Scene scene = new Scene(topHBox, 630, 750);
    scene.getStylesheets().add("/styles.css");

    enableFileDragging(scene);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
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
          String filePath = null;
          for (File file : db.getFiles()) {
            // initializeGame(Paths.get(file.getAbsolutePath()));
          }
        }
        event.setDropCompleted(success);
        event.consume();
      }
    });
  }
}
