package com.toomasr.sgf4j.board;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class BoardStone extends StackPane {
  public static final int width = 29;

  private final int x;
  private final int y;

  private StoneState squareState = StoneState.EMPTY;

  private double strokeWidth = 1.5;

  private Circle highLightCircle = new Circle(15, 15, 10);
  // main visual stone - a circle shape
  private Circle stoneCircle = new Circle(15, 15, 12.5);

  private Text text;
  private Line lineH;
  private Line lineV;

  private Circle starPoint;

  public BoardStone(int x, int y) {
    super();

    this.x = x;
    this.y = y;

    setSize(width);
    setAlignment(Pos.CENTER);

    // some defaults
    highLightCircle.setStrokeType(StrokeType.INSIDE);
    highLightCircle.setStrokeWidth(strokeWidth);

    init();
  }

  private void setSize(int width) {
    setMinWidth(width);
    setMaxWidth(width);
    setMinHeight(width);
    setMaxHeight(width);
  }

  private void init() {
    // the board stone is based on a white rectangle
    Rectangle rect = new Rectangle(width, width);
    rect.setFill(Color.WHITE);
    getChildren().add(rect);

    // we add the lines that make up the intersection on each rectangle
    addTheBgIntersection();
  }

  private void addTheBgIntersection() {
    lineH = new Line(0, width / 2, width, width / 2);
    if (x == 1) {
      // the + 1 is to compensate for the stroke width overflow
      lineH = new Line(width / 2 + 1, width / 2, width, width / 2);
      getChildren().add(lineH);
      setAlignment(lineH, Pos.CENTER_RIGHT);
    }
    else if (x == 19) {
      lineH = new Line(width / 2, width / 2, width - 1, width / 2);
      getChildren().add(lineH);
      setAlignment(lineH, Pos.CENTER_LEFT);
    }
    else {
      getChildren().add(lineH);
      setAlignment(lineH, Pos.CENTER);
    }

    lineV = new Line(width / 2, 0, width / 2, width);
    if (y == 1) {
      // the -1 is compensate for the stroke width overflow
      lineV = new Line(width / 2, width / 2 - 1, width / 2, 0);
      getChildren().add(lineV);
      setAlignment(lineV, Pos.BOTTOM_CENTER);
    }
    else if (y == 19) {
      lineV = new Line(width / 2, width / 2, width / 2, width - 1);
      getChildren().add(lineV);
      setAlignment(lineV, Pos.TOP_CENTER);
    }
    else {
      getChildren().add(lineV);
      setAlignment(lineV, Pos.CENTER);
    }

    if ((x == 4 && y == 4) || (x == 16 && y == 4)
        || (x == 4 && y == 16) || (x == 16 && y == 16)
        || (x == 10 && y == 4) || (x == 10 && y == 16)
        || (x == 4 && y == 10) || (x == 16 && y == 10)
        || (x == 10 && y == 10)) {
      starPoint = new Circle(3, 3, 3);
      starPoint.setStroke(Color.BLACK);
      getChildren().add(starPoint);
    }
  }

  public void removeStone() {
    squareState = StoneState.EMPTY;
    stoneCircle.setVisible(false);
    highLightCircle.setVisible(false);
  }

  public void addOverlayText(String str) {
    text = new Text(str);
    Font font = Font.font(Font.getDefault().getName(), FontWeight.MEDIUM, 16);
    text.setFont(font);
    text.setStroke(Color.SADDLEBROWN);
    text.setFill(Color.SADDLEBROWN);
    setAlignment(text, Pos.CENTER);
    getChildren().add(text);

    lineH.setVisible(false);
    lineV.setVisible(false);

    if (starPoint != null) {
      starPoint.setVisible(false);
    }
  }

  public void removeOverlayText() {
    if (getChildren().contains(text)) {
      getChildren().remove(text);
    }

    lineH.setVisible(true);
    lineV.setVisible(true);

    if (starPoint != null) {
      starPoint.setVisible(true);
    }
  }

  public void placeStone(StoneState stoneState) {
    this.squareState = stoneState;

    stoneCircle.setStroke(Color.BLACK);
    stoneCircle.setStrokeType(StrokeType.OUTSIDE);
    stoneCircle.setStrokeWidth(strokeWidth);
    stoneCircle.setVisible(true);

    if (stoneState.equals(StoneState.WHITE)) {
      stoneCircle.setFill(Color.WHITE);
    }
    else {
      stoneCircle.setFill(Color.BLACK);
    }

    stoneCircle.setSmooth(true);

    if (!getChildren().contains(stoneCircle)) {
      getChildren().add(stoneCircle);
    }
  }

  public void highLightStone() {
    if (squareState.equals(StoneState.WHITE)) {
      highLightCircle.setStroke(Color.BLACK);
      highLightCircle.setFill(Color.WHITE);
    }
    else {
      highLightCircle.setStroke(Color.WHITE);
      highLightCircle.setFill(Color.BLACK);
    }

    highLightCircle.setVisible(true);

    if (!getChildren().contains(highLightCircle)) {
      getChildren().add(highLightCircle);
    }
  }

  public void deHighLightStone() {
    if (highLightCircle != null) {
      highLightCircle.setVisible(false);
    }
  }

  @Override
  public String toString() {
    return "BoardStone [x=" + x + ", y=" + y + ", squareState=" + squareState + "]";
  }
}
