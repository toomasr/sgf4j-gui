package com.toomasr.sgf4j.gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.board.BoardPane;
import com.toomasr.sgf4j.board.BoardStone;
import com.toomasr.sgf4j.board.CoordinateSquare;
import com.toomasr.sgf4j.board.GuiBoardListener;
import com.toomasr.sgf4j.board.StoneState;
import com.toomasr.sgf4j.board.VirtualBoard;
import com.toomasr.sgf4j.filetree.FileTreeView;
import com.toomasr.sgf4j.movetree.EmptyTriangle;
import com.toomasr.sgf4j.movetree.GlueStone;
import com.toomasr.sgf4j.movetree.GlueStoneType;
import com.toomasr.sgf4j.movetree.TreeStone;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class MainUI {
  private Button nextButton;
  private GameNode currentMove = null;
  private GameNode prevMove = null;
  private Game game;
  private VirtualBoard virtualBoard;
  private BoardStone[][] board;
  private GridPane movePane;
  private GridPane boardPane;

  private Map<GameNode, TreeStone> nodeToTreeStone = new HashMap<>();

  private TextArea commentArea;

  private Button previousButton;

  private ScrollPane treePaneScrollPane;

  public MainUI() {
    board = new BoardStone[19][19];

    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));
  }

  public Pane buildUI() throws Exception {
    HBox topHBox = new HBox();
    enableKeyboardShortcuts(topHBox);

    VBox fileTreePane = generateFileTreePane();
    fileTreePane.setAlignment(Pos.CENTER);

    topHBox.getChildren().add(fileTreePane);

    GridPane boardPane = generateBoardPane();
    TilePane buttonPane = generateButtonPane();
    VBox treePane = generateMoveTreePane();

    VBox centerVbox = new VBox();
    centerVbox.setMaxWidth(600);
    centerVbox.setAlignment(Pos.CENTER);

    centerVbox.getChildren().add(boardPane);
    centerVbox.getChildren().add(buttonPane);
    centerVbox.getChildren().add(treePane);

    topHBox.getChildren().add(centerVbox);

    VBox mostRightBox = new VBox();
    mostRightBox = generateCommentPane();
    topHBox.getChildren().add(mostRightBox);

    String game = "src/main/resources/game.sgf";
    initializeGame(Paths.get(game));

    return topHBox;
  }

  private VBox generateCommentPane() {
    VBox rtrn = new VBox();

    commentArea = new TextArea();
    commentArea.setFocusTraversable(false);
    commentArea.setWrapText(true);
    commentArea.setPrefSize(300, 600);

    rtrn.getChildren().add(commentArea);

    return rtrn;
  }

  private void initializeGame(Path pathToSgf) {
    this.game = Sgf.createFromPath(pathToSgf);

    currentMove = this.game.getRootNode();
    prevMove = null;

    // reset our virtual board and actual board
    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));

    boardPane.getChildren().clear();

    for (int i = 0; i < 21; i++) {
      if (i > 1 && i < 20) {
        board[i - 1] = new BoardStone[19];
      }

      for (int j = 0; j < 21; j++) {
        if (i == 0 || j == 0 || i == 20 || j == 20) {
          CoordinateSquare btn = new CoordinateSquare(i, j);
          boardPane.add(btn, i, j);
        }
        else {
          BoardStone btn = new BoardStone(i, j);
          boardPane.add(btn, i, j);
          board[i - 1][j - 1] = btn;
        }
      }
    }

    // if there are any moves that should already be on the board
    // then lets make it so
    if (game.getProperty("AB") != null) {
      String[] blackStones = game.getProperty("AB").split(",");
      for (int i = 0; i < blackStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(blackStones[i]);
        virtualBoard.placeStone(StoneState.BLACK, moveCoords[0], moveCoords[1]);
      }
    }

    // and the same story for white
    if (game.getProperty("AW") != null) {
      String[] blackStones = game.getProperty("AW").split(",");
      for (int i = 0; i < blackStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(blackStones[i]);
        virtualBoard.placeStone(StoneState.WHITE, moveCoords[0], moveCoords[1]);
      }
    }

    // construct the tree of the moves
    nodeToTreeStone = new HashMap<>();
    movePane.getChildren().clear();
    movePane.add(new EmptyTriangle(), 1, 0);

    GameNode rootNode = game.getRootNode();
    populateMoveTreePane(rootNode, 0);

    showMarkersForMove(rootNode);
    showCommentForMove(rootNode);
  }

  private void populateMoveTreePane(GameNode node, int depth) {
    // we draw out only actual moves
    if (node.isMove()) {
      TreeStone treeStone = new TreeStone(node);
      movePane.add(treeStone, node.getMoveNo() + 1, node.getVisualDepth());
      nodeToTreeStone.put(node, treeStone);

      treeStone.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          TreeStone stone = (TreeStone) event.getSource();
          fastForwardTo(stone.getMove());
        }
      });

    }

    // and draw the next node on this line of play
    if (node.getNextNode() != null) {
      populateMoveTreePane(node.getNextNode(), depth + node.getVisualDepth());
    }

    // populate the children also
    if (node.hasChildren()) {
      Set<GameNode> children = node.getChildren();

      // will determine whether the glue stone should be a single
      // diagonal or a multiple (diagonal and vertical)
      GlueStoneType gStoneType = children.size() > 1 ? GlueStoneType.MULTIPLE : GlueStoneType.DIAGONAL;

      for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
        GameNode childNode = ite.next();

        // the last glue shouldn't be a MULTIPLE
        if (GlueStoneType.MULTIPLE.equals(gStoneType) && !ite.hasNext()) {
          gStoneType = GlueStoneType.DIAGONAL;
        }
        
        // also draw all the "missing" glue stones
        for (int i = node.getVisualDepth()+2; i < childNode.getVisualDepth(); i++) {
          movePane.add(new GlueStone(GlueStoneType.VERTICAL), node.getMoveNo() + 1, i);
        }
        
        // glue stone for the node
        movePane.add(new GlueStone(gStoneType), node.getMoveNo() + 1, childNode.getVisualDepth());
        // and draw the actual node
        populateMoveTreePane(childNode, depth + childNode.getVisualDepth());
      }
    }
  }

  /*
   * Generates the boilerplate for the move tree pane. The
   * pane is actually populated during game initialization.
   */
  private VBox generateMoveTreePane() {
    VBox rtrn = new VBox();

    movePane = new GridPane();
    movePane.setPadding(new Insets(0, 0, 0, 0));
    movePane.setStyle("-fx-background-color: white");

    treePaneScrollPane = new ScrollPane(movePane);
    treePaneScrollPane.setPrefHeight(150);
    treePaneScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
    treePaneScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

    rtrn.getChildren().add(treePaneScrollPane);

    return rtrn;
  }

  private void fastForwardTo(GameNode move) {
    // clear the board
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        board[i][j].removeStone();
      }
    }

    deHighLightStoneInTree(currentMove);
    removeMarkersForNode(currentMove);

    virtualBoard.fastForwardTo(move);
    highLightStoneOnBoard(move);
  }

  private VBox generateFileTreePane() {
    VBox vbox = new VBox();
    vbox.setPrefWidth(200);
    TreeView<File> treeView = new FileTreeView();
    treeView.setFocusTraversable(false);
    vbox.getChildren().add(treeView);

    treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() == 2) {
          TreeItem<File> item = treeView.getSelectionModel().getSelectedItem();
          if (item.getValue().toPath().toFile().isFile())
            initializeGame(item.getValue().toPath());
        }
      }

    });

    return vbox;
  }

  private TilePane generateButtonPane() {
    TilePane pane = new TilePane();
    pane.setAlignment(Pos.CENTER);
    pane.setMaxWidth(700);
    pane.getStyleClass().add("bordered");

    TextField moveNoField = new TextField("0");
    moveNoField.setFocusTraversable(false);
    moveNoField.setMaxWidth(40);
    moveNoField.setEditable(false);

    nextButton = new Button("Next");
    nextButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        handleNextPressed();
      }
    });

    previousButton = new Button("Previous");
    previousButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        handlePreviousPressed();
      }

    });

    pane.setPrefColumns(1);
    pane.getChildren().add(previousButton);
    pane.getChildren().add(moveNoField);
    pane.getChildren().add(nextButton);
    return pane;
  }

  private void handleNextPressed() {
    if (currentMove.getNextNode() != null) {
      prevMove = currentMove;
      currentMove = currentMove.getNextNode();
      virtualBoard.makeMove(currentMove, prevMove);

      // scroll the scrollpane to make
      // the highlighted move visible
      ensureVisibleForActiveTreeNode(currentMove);
    }
  }

  public void playMove(GameNode move, GameNode prevMove) {
    this.currentMove = move;
    this.prevMove = prevMove;

    // we actually have a previous move!
    if (prevMove != null) {
      // de-highlight previously highlighted move
      if (prevMove.isMove()) {
        deHighLightStoneOnBoard(prevMove);
      }
      // even non moves can haver markers
      removeMarkersForNode(prevMove);
    }

    if (move != null) {
      highLightStoneOnBoard(move);
    }

    // highlight stone in the tree pane
    deHighLightStoneInTree(prevMove);
    highLightStoneInTree(move);

    // show the associated comment
    showCommentForMove(move);

    // handle the prev and new markers

    showMarkersForMove(move);
    nextButton.requestFocus();
  }

  public void handlePreviousPressed() {
    if (currentMove.getParentNode() != null) {
      prevMove = currentMove;
      currentMove = currentMove.getParentNode();

      virtualBoard.undoMove(prevMove, currentMove);
    }
  }

  public void undoMove(GameNode move, GameNode prevMove) {
    this.currentMove = prevMove;
    this.prevMove = move;
    if (prevMove != null) {
      showMarkersForMove(prevMove);
      showCommentForMove(prevMove);
      if (prevMove.isMove())
        highLightStoneOnBoard(prevMove);
    }

    deHighLightStoneInTree(move);
    highLightStoneInTree(prevMove);

    if (move != null) {
      removeMarkersForNode(move);
    }

    ensureVisibleForActiveTreeNode(prevMove);
    // rather have previous move button have focus
    previousButton.requestFocus();
  }

  private void ensureVisibleForActiveTreeNode(GameNode move) {
    if (move != null && move.isMove()) {
      TreeStone stone = nodeToTreeStone.get(move);

      // the movetree is not yet fully operational and some
      // points don't exist in the map yet
      if (stone == null)
        return;

      double width = treePaneScrollPane.getContent().getBoundsInLocal().getWidth();
      double x = stone.getBoundsInParent().getMaxX();
      double scrollTo = ((x) - 11 * 30) / (width - 21 * 30);

      treePaneScrollPane.setHvalue(scrollTo);
    }
  }

  private void highLightStoneInTree(GameNode move) {
    TreeStone stone = nodeToTreeStone.get(move);
    // can remove the null check at one point when the
    // tree is fully implemented
    if (stone != null) {
      stone.highLight();
      stone.requestFocus();
    }
  }

  private void deHighLightStoneInTree(GameNode node) {
    if (node != null && node.isMove()) {
      TreeStone stone = nodeToTreeStone.get(node);
      if (stone != null) {
        stone.deHighLight();
      }
      else {
        throw new RuntimeException("Unable to find node for move " + node);
      }
    }
  }

  private void showCommentForMove(GameNode move) {
    String comment = move.getProperty("C");
    if (comment == null) {
      comment = "";
    }
    // some helpers I used for parsing needs to be undone - see the Parser.java
    // in sgf4j project
    comment = comment.replaceAll("@@@@@", "\\\\\\[");
    comment = comment.replaceAll("#####", "\\\\\\]");

    // lets do some replacing - see http://www.red-bean.com/sgf/sgf4.html#text
    comment = comment.replaceAll("\\\\\n", "");
    comment = comment.replaceAll("\\\\:", ":");
    comment = comment.replaceAll("\\\\\\]", "]");
    comment = comment.replaceAll("\\\\\\[", "[");

    commentArea.setText(comment);
  }

  private void showMarkersForMove(GameNode move) {
    String markerProp = move.getProperty("L");
    if (markerProp != null) {
      int alphaIdx = 0;
      String[] markers = markerProp.split("\\]\\[");
      for (int i = 0; i < markers.length; i++) {
        int[] coords = Util.alphaToCoords(markers[i]);
        board[coords[0]][coords[1]].addOverlayText(Util.alphabet[alphaIdx++]);
      }
    }
  }

  private void removeMarkersForNode(GameNode node) {
    String markerProp = node.getProperty("L");

    if (markerProp != null) {
      String[] markers = markerProp.split("\\]\\[");
      for (int i = 0; i < markers.length; i++) {
        int[] coords = Util.alphaToCoords(markers[i]);
        board[coords[0]][coords[1]].removeOverlayText();
      }
    }
  }

  private void highLightStoneOnBoard(GameNode move) {
    String currentMove = move.getMoveString();
    int[] moveCoords = Util.alphaToCoords(currentMove);
    board[moveCoords[0]][moveCoords[1]].highLightStone();
  }

  private void deHighLightStoneOnBoard(GameNode prevMove) {
    String prevMoveAsStr = prevMove.getMoveString();
    int[] moveCoords = Util.alphaToCoords(prevMoveAsStr);
    board[moveCoords[0]][moveCoords[1]].deHighLightStone();
  }

  private GridPane generateBoardPane() {
    boardPane = new BoardPane(19, 19);

    for (int i = 0; i < 21; i++) {
      if (i > 1 && i < 20) {
        board[i - 1] = new BoardStone[19];
      }

      for (int j = 0; j < 21; j++) {
        if (i == 0 || j == 0 || i == 20 || j == 20) {
          CoordinateSquare btn = new CoordinateSquare(i, j);
          boardPane.add(btn, i, j);
        }
        else {
          BoardStone btn = new BoardStone(i, j);
          boardPane.add(btn, i, j);
          board[i - 1][j - 1] = btn;
        }
      }
    }
    return boardPane;
  }

  private void enableKeyboardShortcuts(HBox topHBox) {
    topHBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
          if (event.getCode().equals(KeyCode.LEFT)) {
            handlePreviousPressed();
          }
          else if (event.getCode().equals(KeyCode.RIGHT)) {
            handleNextPressed();
          }
        }
      }
    });
  }

  public BoardStone[][] getBoard() {
    return this.board;
  }
}
