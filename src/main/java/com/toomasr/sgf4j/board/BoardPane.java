package com.toomasr.sgf4j.board;

import javafx.scene.layout.GridPane;

public class BoardPane extends GridPane {
  int width;
  int height;

  public BoardPane(int width, int height) {
    super();

    this.width = width;
    this.height = height;

    setMinWidth(700);
  }
}
