package com.toomasr.sgf4j.movetree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GlueStone extends Canvas {
  public static final int width = 30;

  public GlueStone(GlueStoneType type) {
    super(width, width);

    GraphicsContext gc = this.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.setLineWidth(2.0);
    
    if (GlueStoneType.MULTIPLE.equals(type)) {
      // the diagonal line
      gc.strokeLine(width/2, 0, width, width/2);
      // the vertical line
      gc.strokeLine(width/2, 0, width/2, width);      
    }
    else if (GlueStoneType.DIAGONAL.equals(type)) {
      gc.strokeLine(width/2, 0, width, width/2);      
    }
    else if (GlueStoneType.VERTICAL.equals(type)) {
      gc.strokeLine(width/2, 0, width/2, width);      
    }
  }
}
