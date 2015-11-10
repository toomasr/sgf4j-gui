package com.toomasr.board;

import javafx.scene.layout.GridPane;

public class BoardPane extends GridPane {
  int width;
  int height;

  public BoardPane(int width, int height) {
    super();

    this.width = width;
    this.height = height;

    setMaxWidth(700);
  }
}
