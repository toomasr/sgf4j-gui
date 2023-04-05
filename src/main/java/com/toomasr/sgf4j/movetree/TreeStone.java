package com.toomasr.sgf4j.movetree;

import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.board.StoneState;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TreeStone extends StackPane implements MoveTreeElement {
  public static final int width = 30;

  private StoneState squareState = StoneState.EMPTY;
  private int stonePointWidth = 28;

  private GameNode node;
  private Rectangle rect;

  private boolean drawLeftArrow = true;
  private boolean drawRightArrow = true;

  public TreeStone(GameNode node) {
    this(node, true, true);
  }

  public TreeStone(GameNode node, boolean drawLeftArrow, boolean drawRightArrow) {
    super();
    setFocusTraversable(false);

    this.drawLeftArrow = drawLeftArrow;
    this.drawRightArrow = drawRightArrow;

    if ("W".equals(node.getColor())) {
      this.squareState = StoneState.WHITE;
    }
    else {
      this.squareState = StoneState.BLACK;
    }

    this.node = node;

    placeStone(squareState);
    if (node.getProperty("C") != null) {
      drawCommentMarker();
    }

    getStyleClass().add("tree-stone");
  }

  private void drawRightArrow() {
    Line line = new Line(28, width / 2, 30, width / 2);
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(2d);
    setAlignment(line, Pos.CENTER_RIGHT);
    getChildren().add(line);
  }

  private void drawLeftArrow() {
    Line line = new Line(0, width / 2, 2, width / 2);
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(2d);
    setAlignment(line, Pos.CENTER_LEFT);
    getChildren().add(line);
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
    double radius = 14 - strokeWidth;

    if (state.equals(StoneState.WHITE)) {
      fillColor = Color.WHITE;
      strokeColor = Color.BLACK;
    }

    rect = new Rectangle(width, width);
    rect.setStyle("-fx-fill: tree-stone");
    getChildren().add(rect);

    if (drawRightArrow) {
      drawRightArrow();
    }

    if (drawLeftArrow) {
      drawLeftArrow();
    }

    Circle circle = new Circle(width / 2, width / 2, radius);

    circle.setStroke(Color.BLACK);
    circle.setStrokeWidth(strokeWidth);
    circle.setFill(fillColor);
    getChildren().add(circle);

    Font font = Font.font(Font.getDefault().getFamily(), 12);

    Text text = new Text("" + node.getMoveNo());
    text.setFont(font);
    text.setStroke(strokeColor);
    getChildren().add(text);
  }

  public void highLight() {
    rect.setStyle("-fx-fill: tree-stone-active");
  }

  public void deHighLight() {
    rect.setStyle("-fx-fill: tree-stone");
  }

  public GameNode getMove() {
    return node;
  }

  /**
   * Utility method to create a TreeStone with the proper
   * arrows present. Takes into account whether there is a
   * preceding or a following node.
   *
   * @param node
   * @return a properly initialised TreeStone.
   */
  public static TreeStone create(GameNode node) {
    boolean drawLeftArrow = true;
    boolean drawRightArrow = true;

    // no left arrow if no move preceding
    if (node.getPrevNode() == null) {
      drawLeftArrow = false;
    }

    // no right arrow if no move following
    if (node.getNextNode() == null || (node.getNextNode() != null && !node.getNextNode().isMove())) {
      drawRightArrow = false;
    }

    TreeStone treeStone = new TreeStone(node, drawLeftArrow, drawRightArrow);
    return treeStone;
  }

  @Override
  public String toString() {
    return "TreeStone [squareState=" + squareState + ", stonePointWidth=" + stonePointWidth + ", moveNo=" + node.getMoveNo() + "]";
  }
}
