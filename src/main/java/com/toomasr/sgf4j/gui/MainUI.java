package com.toomasr.sgf4j.gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.SGF4JApp;
import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.SgfProperties;
import com.toomasr.sgf4j.board.BoardCoordinateLabel;
import com.toomasr.sgf4j.board.BoardSquare;
import com.toomasr.sgf4j.board.GuiBoardListener;
import com.toomasr.sgf4j.board.StoneState;
import com.toomasr.sgf4j.board.VirtualBoard;
import com.toomasr.sgf4j.filetree.FileTreeView;
import com.toomasr.sgf4j.movetree.GameStartNoopStone;
import com.toomasr.sgf4j.movetree.GlueStone;
import com.toomasr.sgf4j.movetree.GlueStoneType;
import com.toomasr.sgf4j.movetree.MoveTreeElement;
import com.toomasr.sgf4j.movetree.TreeStone;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;
import com.toomasr.sgf4j.properties.AppState;
import com.toomasr.sgf4j.util.Encoding;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainUI {
  private static final Logger logger = LoggerFactory.getLogger(MainUI.class);

  private Button nextButton;
  private GameNode currentMove = null;
  private GameNode prevMove = null;
  private Game game;
  private VirtualBoard virtualBoard;
  private BoardSquare[][] board;
  private GridPane movePane;
  private GridPane boardPane;

  private Map<GameNode, MoveTreeElement> nodeToTreeStone = new HashMap<>();
  private List<MoveTreeElement> highlightedTreeStone = new ArrayList<>();

  private TextArea commentArea;

  private Button previousButton;

  private ScrollPane treePaneScrollPane;
  private Label whitePlayerName;
  private Label blackPlayerName;
  private Label label;

  private TilePane buttonPane;

  private ScrollPane treePane;

  private VBox leftVBox;

  private VBox rightVBox;

  private SGF4JApp app;

  public MainUI(SGF4JApp app) {
    this.app = app;

    board = new BoardSquare[19][19];

    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));
  }

  public Pane buildUI() throws Exception {
    /*
     * --------------------------
     * | | | |
     * | left | center | right |
     * | | | |
     * --------------------------
     */
    Insets paneInsets = new Insets(5, 0, 0, 0);

    leftVBox = new VBox(5);
    leftVBox.setPadding(paneInsets);

    VBox centerVBox = new VBox(5);
    centerVBox.setPadding(paneInsets);

    rightVBox = new VBox(5);
    rightVBox.setPadding(paneInsets);

    // constructing the left box
    VBox fileTreePane = generateFileTreePane();
    leftVBox.setMaxWidth(450);
    leftVBox.getChildren().addAll(fileTreePane);
    VBox.setVgrow(fileTreePane, Priority.ALWAYS);
    HBox.setHgrow(fileTreePane, Priority.SOMETIMES);

    // constructing the center box
    // centerVBox.setMaxWidth(640);
    centerVBox.setMinWidth(640);

    boardPane = new GridPane();
    boardPane.setAlignment(Pos.BASELINE_CENTER);
    boardPane.setHgap(0.0);
    boardPane.setVgap(0.0);

    generateBoardPane(boardPane);

    buttonPane = generateButtonPane();
    treePane = generateMoveTreePane();

    centerVBox.getChildren().addAll(boardPane, buttonPane, treePane);
    VBox.setVgrow(boardPane, Priority.ALWAYS);
    HBox.setHgrow(boardPane, Priority.ALWAYS);

    VBox.setVgrow(treePane, Priority.ALWAYS);
    VBox.setVgrow(buttonPane, Priority.NEVER);

    // constructing the right box
    VBox gameMetaInfo = generateGameMetaInfo();
    TextArea commentArea = generateCommentPane();
    rightVBox.getChildren().addAll(gameMetaInfo, commentArea);
    rightVBox.setMaxWidth(450);
    VBox.setVgrow(commentArea, Priority.ALWAYS);

    // lets put everything into a rootbox!
    HBox rootHBox = new HBox();
    enableKeyboardShortcuts(rootHBox);
    rootHBox.getChildren().addAll(leftVBox, centerVBox, rightVBox);
    HBox.setHgrow(centerVBox, Priority.ALWAYS);
    HBox.setHgrow(leftVBox, Priority.ALWAYS);
    HBox.setHgrow(rightVBox, Priority.SOMETIMES);

    MenuBar menuBar = buildTopMenu();

    VBox rootVbox = new VBox(menuBar);
    HBox statusBar = generateStatusBar();
    rootVbox.getChildren().addAll(rootHBox, statusBar);
    VBox.setVgrow(rootHBox, Priority.ALWAYS);
    VBox.setVgrow(statusBar, Priority.NEVER);

    rootVbox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
      int newSize = resizeBoardPane(boardPane, oldValue, newValue);
      buttonPane.setPrefWidth(newSize * 21);
      treePane.setPrefWidth(newSize * 21);
    });

    return rootVbox;
  }

  private MenuBar buildTopMenu() {
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem restartUIMenuItem = new MenuItem("Restart UI");

    restartUIMenuItem.setOnAction(e -> {
      System.out.println("Restarting UI");
      app.scheduleRestartUI();
    });

    fileMenu.getItems().add(restartUIMenuItem);
    menuBar.getMenus().add(fileMenu);
    return menuBar;
  }

  private HBox generateStatusBar() {
    HBox rtrn = new HBox();

    label = new Label("MainUI loaded");
    rtrn.getChildren().add(label);

    return rtrn;
  }

  public void updateStatus(String update) {
    this.label.setText(update);
  }

  public void initGame() {
    String game = "src/main/resources/game.sgf";
    Path path = Paths.get(game);
    // in development it is nice to have a game open on start
    if (path.toFile().exists()) {
      initializeGame(Paths.get(game));
    }
  }

  private VBox generateGameMetaInfo() {
    VBox vbox = new VBox();

    vbox.setMinWidth(250);
    GridPane pane = new GridPane();

    Label blackPlayerLabel = new Label("Black:");
    GridPane.setConstraints(blackPlayerLabel, 1, 0);

    blackPlayerName = new Label("Unknown");
    GridPane.setConstraints(blackPlayerName, 2, 0);

    Label whitePlayerLabel = new Label("White:");
    GridPane.setConstraints(whitePlayerLabel, 1, 1);

    whitePlayerName = new Label("Unknown");
    GridPane.setConstraints(whitePlayerName, 2, 1);

    pane.getChildren().addAll(blackPlayerLabel, blackPlayerName, whitePlayerLabel, whitePlayerName);

    vbox.getChildren().add(pane);
    return vbox;
  }

  private TextArea generateCommentPane() {
    commentArea = new TextArea();
    commentArea.setFocusTraversable(false);
    commentArea.setWrapText(true);
    commentArea.setPrefSize(300, 600);

    return commentArea;
  }

  private void initializeGame(Path pathToSgf) {
    Font font = Font.getDefault();
    java.awt.Font awtFont = new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int) font.getSize());
    String encoding = Encoding.determineEncoding(pathToSgf, awtFont);
    logger.debug("Determined encoding {}", encoding);
    updateStatus(String.format("Loaded %s with encoding %s", pathToSgf.getFileName(), encoding));
    this.game = Sgf.createFromPath(pathToSgf, encoding);

    currentMove = this.game.getRootNode();
    prevMove = null;

    // reset our virtual board and actual board
    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));

    initNewBoard();

    // construct the tree of the moves
    nodeToTreeStone = new HashMap<>();
    movePane.getChildren().clear();
    GameStartNoopStone rootStone = new GameStartNoopStone(currentMove);
    movePane.add(rootStone, 0, 0);
    configureMoveTreeElement(currentMove, rootStone);
    highLightStoneInTree(currentMove);

    GameNode rootNode = game.getRootNode();
    populateMoveTreePane(rootNode, 0);

    showMarkersForMove(rootNode);
    showCommentForMove(rootNode);

    showMetaInfoForGame(this.game);

    treePaneScrollPane.setHvalue(0);
    treePaneScrollPane.setVvalue(0);
  }

  private void showMetaInfoForGame(Game game) {
    String whiteRating = game.getProperty(SgfProperties.WHITE_PLAYER_RATING);
    String whiteLabel = game.getProperty(SgfProperties.WHITE_PLAYER_NAME);
    if (whiteRating != null) {
      whiteLabel = whiteLabel + " [" + whiteRating + "]";
    }
    whitePlayerName.setText(whiteLabel);

    String blackRating = game.getProperty(SgfProperties.BLACK_PLAYER_RATING);
    String blackLabel = game.getProperty(SgfProperties.BLACK_PLAYER_NAME);
    if (blackRating != null) {
      blackLabel = blackLabel + " [" + blackRating + "]";
    }
    blackPlayerName.setText(blackLabel);
  }

  public void initNewBoard() {
    generateBoardPane(boardPane);
    placePreGameStones(game);
  }

  private void placePreGameStones(Game game) {
    String blackStones = game.getProperty("AB", "");
    String whiteStones = game.getProperty("AW", "");

    placePlacementGameStones(blackStones, whiteStones);
  }

  private void placePlacementStones(GameNode node) {
    String blackStones = node.getProperty("AB", "");
    String whiteStones = node.getProperty("AW", "");

    placePlacementGameStones(blackStones, whiteStones);
  }

  private void placePlacementGameStones(String addBlack, String addWhite) {
    if (addBlack.length() > 0) {
      String[] blackStones = addBlack.split(",");
      // actually the stones can also contain not just points but sequences
      // of points so instead of a coordinate like dq, dr, ds it might contain
      // dq:ds. Let us translate those to actual single coordinates!
      if (addBlack.contains(":")) {
        blackStones = Util.coordSequencesToSingle(addBlack);
      }
      for (int i = 0; i < blackStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(blackStones[i]);
        virtualBoard.placeStone(StoneState.BLACK, moveCoords[0], moveCoords[1]);
      }
    }

    if (addWhite.length() > 0) {
      String[] whiteStones = addWhite.split(",");
      // actually the stones can also contain not just points but sequences
      // of points so instead of a coordinate like dq, dr, ds it might contain
      // dq:ds. Let us translate those to actual single coordinates!
      if (addWhite.contains(":")) {
        whiteStones = Util.coordSequencesToSingle(addWhite);
      }
      for (int i = 0; i < whiteStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(whiteStones[i]);
        virtualBoard.placeStone(StoneState.WHITE, moveCoords[0], moveCoords[1]);
      }
    }
  }

  private void removePreGameStones(GameNode node) {
    String blackStones = node.getProperty("AB", "");
    String whiteStones = node.getProperty("AW", "");

    removePlacementStones(blackStones, whiteStones);
  }

  private void removePlacementStones(String removeBlack, String removeWhite) {
    if (removeBlack.length() > 0) {
      String[] blackStones = removeBlack.split(",");
      for (int i = 0; i < blackStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(blackStones[i]);
        virtualBoard.removeStone(moveCoords[0], moveCoords[1]);
      }
    }

    if (removeWhite.length() > 0) {
      String[] whiteStones = removeWhite.split(",");
      for (int i = 0; i < whiteStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(whiteStones[i]);
        virtualBoard.removeStone(moveCoords[0], moveCoords[1]);
      }
    }

  }

  private void populateMoveTreePane(GameNode node, int depth) {
    // we draw out only actual moves
    if (node.isMove() || (node.getMoveNo() == -1 && node.getVisualDepth() > -1)) {
      MoveTreeElement treeStone = TreeStone.create(node);
      if (node.getMoveNo() == -1) {
        treeStone = new GameStartNoopStone(node);
      }
      movePane.add((StackPane) treeStone, node.getNodeNo(), node.getVisualDepth());
      configureMoveTreeElement(node, treeStone);
    }

    // and recursively draw the next node on this line of play
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

        // the visual lines can also be under a the first triangle
        int nodeVisualDepth = node.getVisualDepth();
        int moveNo = node.getNodeNo();

        if (moveNo == -1) {
          moveNo = 0;
          nodeVisualDepth = 0;
        }
        else if (nodeVisualDepth == -1) {
          nodeVisualDepth = 0;
        }

        // also draw all the "missing" glue stones
        for (int i = nodeVisualDepth + 1; i < childNode.getVisualDepth(); i++) {
          movePane.add(new GlueStone(GlueStoneType.VERTICAL), moveNo, i);
        }

        // glue stone for the node
        movePane.add(new GlueStone(gStoneType), moveNo, childNode.getVisualDepth());

        // and recursively draw the actual node
        populateMoveTreePane(childNode, depth + childNode.getVisualDepth());
      }
    }
  }

  private void configureMoveTreeElement(GameNode node, MoveTreeElement treeStone) {
    nodeToTreeStone.put(node, treeStone);
    ((StackPane) treeStone).addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        MoveTreeElement stone = (MoveTreeElement) event.getSource();
        fastForwardTo(stone.getMove());
      }
    });
  }

  /*
   * Generates the boilerplate for the move tree pane. The
   * pane is actually populated during game initialization.
   */
  private ScrollPane generateMoveTreePane() {

    movePane = new GridPane();
    movePane.setPadding(new Insets(0, 0, 0, 0));
    movePane.setStyle("-fx-background-color: white");

    treePaneScrollPane = new ScrollPane(movePane);
    treePaneScrollPane.setPrefHeight(150);
    treePaneScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    treePaneScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

    movePane.setMinWidth(640);
    movePane.setMaxWidth(Control.USE_PREF_SIZE);

    return treePaneScrollPane;
  }

  private void fastForwardTo(GameNode move) {
    // clear the board
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        board[i][j].removeStone();
      }
    }

    placePreGameStones(game);

    deHighLightStoneInTree();
    removeMarkersForNode(currentMove);

    if (move.getMoveNo() != -1) {
      virtualBoard.fastForwardTo(move);
      highLightStoneOnBoard(move);
    }
    else {
      virtualBoard.fastForwardTo(move);
      highLightStoneInTree(move);
    }
  }

  private VBox generateFileTreePane() {
    VBox vbox = new VBox();
    vbox.setMinWidth(250);

    TreeView<File> treeView = new FileTreeView();
    treeView.setFocusTraversable(false);

    Label label = new Label("Choose SGF File");
    vbox.getChildren().addAll(label, treeView);
    VBox.setVgrow(treeView, Priority.ALWAYS);

    treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() == 2) {
          TreeItem<File> item = treeView.getSelectionModel().getSelectedItem();
          File file = item.getValue().toPath().toFile();
          if (file.isFile()) {
            initializeGame(item.getValue().toPath());
          }
          AppState.getInstance().addProperty(AppState.CURRENT_FILE, file.getAbsolutePath());
        }
      }

    });

    return vbox;
  }

  private TilePane generateButtonPane() {
    TilePane pane = new TilePane();
    pane.setAlignment(Pos.CENTER);
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

    pane.setMaxWidth(Control.USE_PREF_SIZE);

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

  private void handleNextBranch() {
    if (currentMove.hasChildren()) {
      prevMove = currentMove;
      currentMove = currentMove.getChildren().iterator().next();
      virtualBoard.makeMove(currentMove, prevMove);

      // scroll the scrollpane to make
      // the highlighted move visible
      ensureVisibleForActiveTreeNode(currentMove);
    }
  }

  public void handlePreviousPressed() {
    if (currentMove.getParentNode() != null) {
      prevMove = currentMove;
      currentMove = currentMove.getParentNode();

      virtualBoard.undoMove(prevMove, currentMove);
    }
  }

  public void playMove(GameNode move, GameNode prevMove) {
    this.currentMove = move;
    this.prevMove = prevMove;

    // we actually have a previous move!
    if (prevMove != null) {
      // de-highlight previously highlighted move
      if (prevMove.isMove() && !prevMove.isPass()) {
        deHighLightStoneOnBoard(prevMove);
      }
      // even non moves can haver markers
      removeMarkersForNode(prevMove);
    }

    if (move != null && !move.isPass() && !move.isPlacementMove() && move.getMoveString() != null) {
      highLightStoneOnBoard(move);
    }

    // highlight stone in the tree pane
    deHighLightStoneInTree();
    highLightStoneInTree(move);

    if (move != null && (move.getProperty("AB") != null || move.getProperty("AW") != null)) {
      placePlacementStones(move);
    }

    // show the associated comment
    showCommentForMove(move);

    // handle the prev and new markers

    showMarkersForMove(move);
    nextButton.requestFocus();
  }

  public void undoMove(GameNode move, GameNode prevMove) {
    this.currentMove = prevMove;
    this.prevMove = move;

    if (move != null) {
      removeMarkersForNode(move);
    }

    if (prevMove != null) {
      showMarkersForMove(prevMove);
      showCommentForMove(prevMove);
      if (prevMove.isMove() && !prevMove.isPass())
        highLightStoneOnBoard(prevMove);
    }

    removePreGameStones(move);

    deHighLightStoneInTree();
    highLightStoneInTree(prevMove);

    ensureVisibleForActiveTreeNode(prevMove);
    // rather have previous move button have focus
    previousButton.requestFocus();
  }

  private void ensureVisibleForActiveTreeNode(GameNode move) {
    if (move != null && move.isMove()) {
      StackPane stone = (StackPane) nodeToTreeStone.get(move);

      // the move tree is not yet fully operational and some
      // points don't exist in the map yet
      if (stone == null)
        return;

      double width = treePaneScrollPane.getContent().getBoundsInLocal().getWidth();
      double x = stone.getBoundsInParent().getMaxX();
      double scrollTo = ((x) - 11 * 30) / (width - 21 * 30);

      treePaneScrollPane.setHvalue(scrollTo);

      // adjust the vertical scroll
      double height = treePaneScrollPane.getContent().getBoundsInLocal().getHeight();
      double y = stone.getBoundsInParent().getMaxY();
      double scrollToY = y / height;

      if (move.getVisualDepth() == 0) {
        scrollToY = 0d;
      }

      treePaneScrollPane.setVvalue(scrollToY);
    }
  }

  private void highLightStoneInTree(GameNode move) {
    MoveTreeElement stone = nodeToTreeStone.get(move);
    // can remove the null check at one point when the
    // tree is fully implemented
    if (stone != null) {
      stone.highLight();
      stone.requestFocus();
      highlightedTreeStone.add(stone);
    }
  }

  private void deHighLightStoneInTree(GameNode node) {
    if (node != null && node.isMove()) {
      MoveTreeElement stone = nodeToTreeStone.get(node);
      if (stone != null) {
        stone.deHighLight();
      }
      else {
        throw new RuntimeException("Unable to find node for move " + node);
      }
    }
  }

  private void deHighLightStoneInTree() {
    for (MoveTreeElement stone : highlightedTreeStone) {
      stone.deHighLight();
    }
    highlightedTreeStone.clear();
  }

  private void showCommentForMove(GameNode move) {
    String comment = move.getProperty("C");
    if (comment == null) {
      if (move.getParentNode() == null && game.getProperty("C") != null) {
        comment = game.getProperty("C");
      }
      else {
        comment = "";
      }
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
    // the L property is actually not used in FF3 and FF4
    // but I own many SGFs that still have it
    String markerProp = move.getProperty("L");
    if (markerProp != null) {
      int alphaIdx = 0;
      String[] markers = markerProp.split("\\]\\[");
      for (int i = 0; i < markers.length; i++) {
        int[] coords = Util.alphaToCoords(markers[i]);
        board[coords[0]][coords[1]].addOverlayText(Util.alphabet[alphaIdx++]);
      }
    }

    // also handle the LB labels
    Map<String, String> labels = Util.extractLabels(move.getProperty("LB"));
    for (Iterator<Map.Entry<String, String>> ite = labels.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      int[] coords = Util.alphaToCoords(entry.getKey());
      board[coords[0]][coords[1]].addOverlayText(entry.getValue());
    }
  }

  private void removeMarkersForNode(GameNode node) {
    // the L property is actually not used in FF3 and FF4
    // but I own many SGFs that still have it
    String markerProp = node.getProperty("L");

    if (markerProp != null) {
      String[] markers = markerProp.split("\\]\\[");
      for (int i = 0; i < markers.length; i++) {
        int[] coords = Util.alphaToCoords(markers[i]);
        board[coords[0]][coords[1]].removeOverlayText();
      }
    }

    // also handle the LB labels
    Map<String, String> labels = Util.extractLabels(node.getProperty("LB"));
    for (Iterator<Map.Entry<String, String>> ite = labels.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      int[] coords = Util.alphaToCoords(entry.getKey());
      board[coords[0]][coords[1]].removeOverlayText();
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

  private void generateBoardPane(GridPane boardPane) {
    boardPane.getChildren().clear();

    for (int i = 0; i < 21; i++) {
      if (i > 1 && i < 20) {
        board[i - 1] = new BoardSquare[19];
      }

      for (int j = 0; j < 21; j++) {
        if (i == 0 || j == 0 || i == 20 || j == 20) {
          BoardCoordinateLabel btn = new BoardCoordinateLabel(i, j);
          boardPane.add(btn, i, j);
        }
        else {
          BoardSquare btn = new BoardSquare(i, j);
          boardPane.add(btn, i, j);
          board[i - 1][j - 1] = btn;
        }
      }
    }
  }

  private int resizeBoardPane(GridPane boardPane, Bounds oldValue, Bounds newValue) {
    // I calculate the high because for some reason the
    // rightVBox and leftVBox in certain circumstances will
    // overflow the centerHBox - by subtracting from total
    // I'll get the visual width of the center box
    double width = newValue.getWidth() - leftVBox.getWidth() - rightVBox.getWidth();
    double height = boardPane.getHeight();

    // when resized super quickly with the mouse then
    // sometimes the width can become negative even
    // this is dumb but just in case lets have a minimum
    if (width < 650) {
      width = 650;
    }

    int newSize = (int) Math.floor(height / 21);
    if ((int) Math.floor(width / 21) < newSize)
      newSize = (int) Math.floor(width / 21);

    // anything less than 29 will look ugly with the
    // current code
    if (newSize < 29) {
      newSize = 29;
    }

    BoardSquare stone = (BoardSquare) boardPane.getChildren().get(23);
    // if size actually hasn't changed then no need to resize everything
    if (stone.getSize() == newSize) {
      return newSize;
    }

    for (int i = 0; i < 21 * 21; i++) {
      if ((i < 22 || i > 418 || i % 21 == 0 || (i + 1) % 21 == 0)) {
        BoardCoordinateLabel sq = (BoardCoordinateLabel) boardPane.getChildren().get(i);
        sq.resizeTo(newSize);
      }
      else {
        stone = (BoardSquare) boardPane.getChildren().get(i);
        stone.resizeTo(newSize);
      }

    }
    return newSize;
  }

  private void enableKeyboardShortcuts(HBox topHBox) {
    topHBox.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
          if (event.getCode().equals(KeyCode.LEFT)) {
            handlePreviousPressed();
          }
          else if (event.getCode().equals(KeyCode.RIGHT)) {
            handleNextPressed();
          }
          else if (event.getCode().equals(KeyCode.DOWN)) {
            handleNextBranch();
          }
        }
      }
    });
  }

  public BoardSquare[][] getBoard() {
    return this.board;
  }
}
