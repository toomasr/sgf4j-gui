package com.toomasr.sgf4j.movetree;

import com.toomasr.sgf4j.board.StoneState;
import com.toomasr.sgf4j.parser.GameNode;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class TreeStone extends StackPane {
  public static final int width = 30;

  private StoneState squareState = StoneState.EMPTY;
  private int stonePointWidth = 28;

  private GameNode node;

  private Rectangle rect;

  public TreeStone(GameNode node) {
    super();

    setFocusTraversable(false);

    if ("W".equals(node.getColor()))
      this.squareState = StoneState.WHITE;
    else
      this.squareState = StoneState.BLACK;

    this.node = node;

    placeStone(squareState);
    if (node.getProperty("C") != null) {
      drawCommentMarker();
    }
  }

  private void drawCommentMarker() {
    Line line = new Line(0, width, width, width);
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(1d);
    setAlignment(line, Pos.BOTTOM_CENTER);
    getChildren().add(line);
  }

  public void placeStone(StoneState state) {
    squareState = state;

    getChildren().clear();

    Color fillColor = Color.BLACK;
    Color strokeColor = Color.WHITE;
    double strokeWidth = 1.5;
    double radius = 15 - strokeWidth;

    if (state.equals(StoneState.WHITE)) {
      fillColor = Color.WHITE;
      strokeColor = Color.BLACK;
    }

    rect = new Rectangle(width, width);
    rect.setFill(Color.WHITE);
    getChildren().add(rect);

    Circle circle = new Circle(15, 15, radius);

    circle.setStroke(Color.BLACK);
    circle.setStrokeWidth(strokeWidth);
    circle.setFill(fillColor);
    getChildren().add(circle);

    Text text = new Text("" + node.getMoveNo());
    text.setStroke(strokeColor);
    getChildren().add(text);
  }

  public void highLight() {
    rect.setFill(Color.SADDLEBROWN);
  }

  public void deHighLight() {
    rect.setFill(Color.WHITE);
  }

  public GameNode getMove() {
    return node;
  }

  @Override
  public String toString() {
    return "TreeStone [squareState=" + squareState + ", stonePointWidth=" + stonePointWidth + ", moveNo=" + node.getMoveNo() + "]";
  }
}
