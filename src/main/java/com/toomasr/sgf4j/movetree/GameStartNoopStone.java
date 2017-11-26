package com.toomasr.sgf4j.movetree;

import java.util.UUID;

import com.toomasr.sgf4j.board.StoneState;
import com.toomasr.sgf4j.parser.GameNode;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameStartNoopStone extends StackPane implements MoveTreeElement {
  public static final int width = 30;

  private StoneState squareState = StoneState.EMPTY;
  private int stonePointWidth = 28;

  private GameNode node;
  private Rectangle rect;
  private String uuid = UUID.randomUUID().toString();

  public GameStartNoopStone(GameNode node) {
    super();
    setFocusTraversable(false);

    this.node = node;
    init();
  }

  private void init() {
    getChildren().clear();

    Color fillColor = Color.ORANGE;
    Color strokeColor = Color.WHITE;
    double strokeWidth = 1.5;
    double radius = 14 - strokeWidth;

    rect = new Rectangle(width, width);
    rect.setFill(Color.WHITE);
    getChildren().add(rect);

    drawRightArrow();

    Circle circle = new Circle(width / 2, width / 2, radius);

    circle.setStroke(Color.BLACK);
    circle.setStrokeWidth(strokeWidth);
    circle.setFill(fillColor);
    getChildren().add(circle);

    Font font = Font.font(Font.getDefault().getFamily(), 12);

    Text text = new Text("S");
    text.setFont(font);
    text.setStroke(strokeColor);
    getChildren().add(text);
  }

  private void drawRightArrow() {
    Line line = new Line(28, width / 2, 30, width / 2);
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(2d);
    setAlignment(line, Pos.CENTER_RIGHT);
    getChildren().add(line);
  }

  public GameNode getMove() {
    return node;
  }

  public void highLight() {
    rect.setFill(Color.SADDLEBROWN);
  }

  public void deHighLight() {
    rect.setFill(Color.WHITE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((node == null) ? 0 : node.hashCode());
    result = prime * result + ((rect == null) ? 0 : rect.hashCode());
    result = prime * result + ((squareState == null) ? 0 : squareState.hashCode());
    result = prime * result + stonePointWidth;
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GameStartNoopStone other = (GameStartNoopStone) obj;
    if (node == null) {
      if (other.node != null)
        return false;
    }
    else if (!node.equals(other.node))
      return false;
    if (rect == null) {
      if (other.rect != null)
        return false;
    }
    else if (!rect.equals(other.rect))
      return false;
    if (squareState != other.squareState)
      return false;
    if (stonePointWidth != other.stonePointWidth)
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    }
    else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }
}
