package com.toomasr.sgf4j.movetree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GlueStone extends Canvas {
  public static final int width = 30;

  public GlueStone() {
    super(width, width);

    GraphicsContext gc = this.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.setLineWidth(2.0);
    gc.strokeLine(width/2, 0, width, width/2);
  }
}
