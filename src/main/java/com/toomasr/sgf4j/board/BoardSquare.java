package com.toomasr.sgf4j.board;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class BoardSquare extends StackPane {
  private static final Image blackStoneImage = new Image(BoardSquare.class.getResourceAsStream("/stones/regular/black-stone.svg"));
  private static final Image blackStoneHighlightedImage = new Image(BoardSquare.class.getResourceAsStream("/stones/regular/black-stone-highlighted.svg"));
  private static final Image whiteStoneImage = new Image(BoardSquare.class.getResourceAsStream("/stones/regular/white-stone.svg"));
  private static final Image whiteStoneHighlightedImage = new Image(BoardSquare.class.getResourceAsStream("/stones/regular/white-stone-highlighted.svg"));
  public static int width = 29;

  private static final double FONT_MULTIPLIER = 1.8125;

  private final int x;
  private final int y;

  private StoneState squareState = StoneState.EMPTY;

  private Text text;
  private Line lineH;
  private Line lineV;

  private Circle starPoint;
  private ImageView stoneImage;

  public BoardSquare(int x, int y) {
    super();

    this.x = x;
    this.y = y;

    getStyleClass().add("board-square"); 

    addTheBgIntersection();
  }

	private void addTheBgIntersection() {
    if (lineH != null) {
      getChildren().remove(lineH);
    }

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

    if (lineV != null) {
      getChildren().remove(lineV);
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

    if (starPoint != null) {
      getChildren().remove(starPoint);
    }

    if ((x == 4 && y == 4) || (x == 16 && y == 4) || (x == 4 && y == 16) || (x == 16 && y == 16) || (x == 10 && y == 4)
        || (x == 10 && y == 16) || (x == 4 && y == 10) || (x == 16 && y == 10) || (x == 10 && y == 10)) {
      starPoint = new Circle(3, 3, 3);
      starPoint.setStroke(Color.BLACK);
      getChildren().add(starPoint);
    }
  }
  
  public StoneState getState() {
    return this.squareState;
  }
  
  public int getX() {
    return this.x;
  }
  
  public int getY() {
    return this.y;
  }

  public void removeStone() {
    squareState = StoneState.EMPTY;
    getChildren().remove(stoneImage);
  }

  public void addOverlayText(String str) {
    // there are SGF files that place multiple labels
    // on a single square - right now won't support that
    // and always showing the latest one
    removeOverlayText();
    text = new Text(str);
    Font font = Font.font(Font.getDefault().getName(), FontWeight.MEDIUM, width / FONT_MULTIPLIER);
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

    getChildren().remove(stoneImage);

    stoneImage = getImageView(blackStoneImage);
    if (stoneState.equals(StoneState.WHITE)) {
      stoneImage = getImageView(whiteStoneImage);
    }

    getChildren().add(stoneImage);
  }

  private ImageView getImageView(Image stoneImage) {
    ImageView rtrn = new ImageView(stoneImage);
    rtrn.setFitWidth(width);
    rtrn.setFitHeight(width);
    return rtrn;
  }

  public void highLightStone() {
    getChildren().remove(stoneImage);

    stoneImage = getImageView(blackStoneHighlightedImage);
    if (squareState.equals(StoneState.WHITE)) {
      stoneImage = getImageView(whiteStoneHighlightedImage);
    }

    getChildren().add(stoneImage);
  }

  public void deHighLightStone() {
    // sometimes deHighlight can be called on a stone
    // that does not exist on the board. Let's not add
    // a stone in that circumstances and exist early
    if (!getChildren().contains(stoneImage))
      return;
    getChildren().remove(stoneImage);

    stoneImage = getImageView(blackStoneImage);

    if (squareState.equals(StoneState.WHITE)) {
      stoneImage = getImageView(whiteStoneImage);
    }

    getChildren().add(stoneImage);
  }

  public int getSize() {
    return width;
  }

  public void resizeTo(int newSize) {
    BoardSquare.width = newSize;
    addTheBgIntersection();

    if (getChildren().contains(stoneImage)) {
      getChildren().remove(stoneImage);
      stoneImage = getImageView(stoneImage.getImage());
      getChildren().add(stoneImage);
    }

    if (getChildren().contains(text)) {
      String str = text.getText();
      removeOverlayText();
      addOverlayText(str);
    }
  }

  public void reset() {
    squareState = StoneState.EMPTY;
    getChildren().clear();
    addTheBgIntersection();
  }

  @Override
  public String toString() {
    return "BoardStone [x=" + x + ", y=" + y + ", squareState=" + squareState + "]";
  }
}
