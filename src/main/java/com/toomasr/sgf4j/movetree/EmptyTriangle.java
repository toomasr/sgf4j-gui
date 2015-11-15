package com.toomasr.sgf4j.movetree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EmptyTriangle extends Canvas {
  public static final int width = 30;

  public EmptyTriangle() {
    super(width, width);

    GraphicsContext gc = this.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    int w1 = 2;
    gc.setLineWidth(w1);
    // the horizontal line
    gc.strokeLine(3, 27, 27, 27);

    gc.setLineWidth(w1);
    // the triangle sides
    gc.strokeLine(3, 27, 15, 5);
    gc.strokeLine(27, 27, 15, 5);

    gc.setFill(Color.BLACK);
    gc.fillOval(13, 17, 4, 4);
  }
}
