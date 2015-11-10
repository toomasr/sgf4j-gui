package com.toomasr.board;

import com.toomasr.sgf4j.parser.Util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class CoordinateSquare extends Canvas {
  public CoordinateSquare(int x, int y) {
    super(BoardStone.width, BoardStone.width);

    // corner square will stay empty
    if ((x == 0 && y == 0) || (x == 20 && y == 20) || (x == 0 && y == 20) || (x == 20 && y == 0)) {
      return;
    }

    GraphicsContext gc = this.getGraphicsContext2D();

    if (x == 0) {
      gc.fillText(y + "", BoardStone.width / 2 - 0.15 * BoardStone.width, BoardStone.width / 2 + 0.1 * BoardStone.width);
    }
    else if (x == 20) {
      gc.fillText(y + "", 0.15 * BoardStone.width, BoardStone.width / 2 + 0.1 * BoardStone.width);
    }
    else if (y == 0) {
      gc.fillText(Util.alphabet[x - 1], BoardStone.width / 3, BoardStone.width / 2 + 0.3 * BoardStone.width);
    }
    else if (y == 20) {
      gc.fillText(Util.alphabet[x - 1], BoardStone.width / 3, BoardStone.width / 3 + 0.2 * BoardStone.width);
    }
  }
}
