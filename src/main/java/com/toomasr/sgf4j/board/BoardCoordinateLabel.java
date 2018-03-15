package com.toomasr.sgf4j.board;

import com.toomasr.sgf4j.parser.Util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class BoardCoordinateLabel extends Canvas {

  private int x;
  private int y;

  public BoardCoordinateLabel(int x, int y) {
    super(BoardSquare.width, BoardSquare.width);
    this.x = x;
    this.y = y;

    init();
  }

  private void init() {
    // corner square will stay empty
    if ((x == 0 && y == 0) || (x == 20 && y == 20) || (x == 0 && y == 20) || (x == 20 && y == 0)) {
      return;
    }

    GraphicsContext gc = this.getGraphicsContext2D();

    if (x == 0) {
      gc.fillText(20-y + "", BoardSquare.width / 2 - 0.15 * BoardSquare.width, BoardSquare.width / 2 + 0.1 * BoardSquare.width);
    }
    else if (x == 20) {
      gc.fillText(20-y + "", 0.15 * BoardSquare.width, BoardSquare.width / 2 + 0.1 * BoardSquare.width);
    }
    else if (y == 0) {
      gc.fillText(Util.alphabet[x - 1], BoardSquare.width / 3, BoardSquare.width / 2 + 0.3 * BoardSquare.width);
    }
    else if (y == 20) {
      gc.fillText(Util.alphabet[x - 1], BoardSquare.width / 3, BoardSquare.width / 3 + 0.2 * BoardSquare.width);
    }
  }

  public void resizeTo(int newSize) {
    setWidth(newSize);
    setHeight(newSize);

    GraphicsContext gc = this.getGraphicsContext2D();
    gc.clearRect(0, 0, newSize, newSize);

    init();
  }
}
