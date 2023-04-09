package com.toomasr.sgf4j.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.SGF4JApp;
import com.toomasr.sgf4j.board.BoardCoordinateLabel;
import com.toomasr.sgf4j.board.BoardSquare;
import com.toomasr.sgf4j.board.GuiBoardListener;
import com.toomasr.sgf4j.filetree.FileTreeView;
import com.toomasr.sgf4j.metasystem.ProblemStatus;
import com.toomasr.sgf4j.movetree.GameStartNoopStone;
import com.toomasr.sgf4j.movetree.GlueStone;
import com.toomasr.sgf4j.movetree.GlueStoneType;
import com.toomasr.sgf4j.movetree.MoveTreeElement;
import com.toomasr.sgf4j.movetree.TreeStone;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.MoveTimingInfo;
import com.toomasr.sgf4j.parser.Sgf;
import com.toomasr.sgf4j.parser.SgfProperties;
import com.toomasr.sgf4j.parser.Util;
import com.toomasr.sgf4j.parser.board.StoneState;
import com.toomasr.sgf4j.parser.board.VirtualBoard;
import com.toomasr.sgf4j.properties.AppState;
import com.toomasr.sgf4j.util.ParserUtils;
import com.toomasr.sgf4j.util.TextUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
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

public class MainUI implements EventHandler<javafx.scene.input.MouseEvent> {
  private static final Logger logger = LoggerFactory.getLogger(MainUI.class);

  private Button nextButton;
  private GameNode currentMove = null;
  private GameNode prevMove = null;
  private MyGame game;
  private VirtualBoard virtualBoard;
  private BoardSquare[][] board;
  private GridPane movePane;
  private GridPane boardPane;

  private Map<Integer, MoveTreeElement> nodeToTreeStone = new HashMap<>();
  private List<MoveTreeElement> highlightedTreeStone = new ArrayList<>();

  private TextArea commentArea;
  private TextField moveNoField = new TextField("0");

  private Button previousButton;

  private ScrollPane treePaneScrollPane;
  private Label whitePlayerName;
  private Label blackPlayerName;

  private TilePane buttonPane;

  private VBox leftVBox;

  private VBox rightVBox;

  private SGF4JApp app;

  private Path activeGameSgf;

  private String activeGameEncoding;

  private VBox rootVbox;

  private Label lastSolved;

  private Label lastOpened;

  private Button[] problemStatusButtons;

  private Label folderInfoLastOpened;

  private Label folderInfoNumberOfProblems;

  private Label folderInfoNumberOfFailed;

  private Label folderInfoNumberOfSolved;
  private FileTreeView fileTreeView;

  private Label statusBarLabel;

  public MainUI(SGF4JApp app) {
    this.app = app;

    board = new BoardSquare[19][19];

    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));
  }

  public Pane buildUI() throws Exception {
    /*
     * -------------------------- | | | | | left | center | right | | | | |
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
    movePane = generateMoveTreePane();
    treePaneScrollPane = generateMoveTreeScrollPane();
    treePaneScrollPane.setContent(movePane);

    centerVBox.getChildren().addAll(boardPane, buttonPane, treePaneScrollPane);
    VBox.setVgrow(boardPane, Priority.ALWAYS);
    HBox.setHgrow(boardPane, Priority.ALWAYS);

    VBox.setVgrow(buttonPane, Priority.NEVER);
    HBox.setHgrow(buttonPane, Priority.ALWAYS);

    // constructing the right box
    VBox gameMetaInfo = generateGameMetaInfoPane();
    TextArea commentArea = generateCommentPane();
    TitledPane tPane = new TitledPane("Comment Area", commentArea);
    
    rightVBox.getChildren().addAll(gameMetaInfo, tPane);
    rightVBox.setMaxWidth(450);
    VBox.setVgrow(commentArea, Priority.ALWAYS);

    // lets put everything into a rootbox!
    HBox rootHBox = new HBox();
    rootHBox.getChildren().addAll(leftVBox, centerVBox, rightVBox);
    HBox.setHgrow(leftVBox, Priority.ALWAYS);
    HBox.setHgrow(rightVBox, Priority.SOMETIMES);

    MenuBar menuBar = buildTopMenu();
    final String os = System.getProperty("os.name");
    if (os != null && os.startsWith("Mac")) {
      menuBar.useSystemMenuBarProperty().set(true);
    }

    rootVbox = new VBox(menuBar);
    HBox statusBar = generateStatusBar();
    rootVbox.getChildren().addAll(rootHBox, statusBar);
    VBox.setVgrow(rootHBox, Priority.ALWAYS);
    VBox.setVgrow(statusBar, Priority.NEVER);

    enableKeyboardShortcuts(rootVbox);
    
    return rootVbox;
  }

  private MenuBar buildTopMenu() {
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem restartUIMenuItem = new MenuItem("Restart UI");

    restartUIMenuItem.setOnAction(e -> {
      app.scheduleRestartUI();
    });

    MenuItem newGame = new MenuItem("New Game");
    newGame.setOnAction(e -> {
      Path temp;
      try {
        temp = Files.createTempFile(null, ".sgf");
        initializeGame(temp);
      } catch (IOException e1) {
        throw new RuntimeException(e1);
      }
    });

    MenuItem saveGame = new MenuItem("Save");
    saveGame.setOnAction(e -> {
      if (saveSgf(this.game.getGame(), this.activeGameSgf, this.activeGameEncoding)) {
        updateStatus("Saved game to " + this.activeGameSgf.getFileName());
      } else {
        updateStatus("File not saved " + this.activeGameSgf.getFileName());
      }

    });

    MenuItem saveGamePosition = new MenuItem("Save as Position");
    saveGamePosition.setOnAction(e -> {
      saveSgfCurrentPosition(this.game.getGame(), this.currentMove, virtualBoard, this.activeGameSgf,
          this.activeGameEncoding);
    });

    fileMenu.getItems().add(newGame);
    fileMenu.getItems().add(saveGame);
    fileMenu.getItems().add(saveGamePosition);

    fileMenu.getItems().add(new SeparatorMenuItem());
    fileMenu.getItems().add(restartUIMenuItem);

    menuBar.getMenus().add(fileMenu);
    return menuBar;
  }

  private boolean saveSgf(Game game, Path outputFile, String encoding) {
    if (activeGameSgf != null && Files.exists(outputFile)) {
      Sgf.writeToFile(game, outputFile, encoding, true);
      return true;
    }
    return false;
  }

  private boolean saveSgfCurrentPosition(Game game, GameNode move, VirtualBoard vBoard, Path origFilePath,
      String encoding) {
    String fileName = origFilePath.getFileName().toString().substring(0,
        origFilePath.getFileName().toString().indexOf("."));
    String copyDescriptor = "-copy";
    Path outputFile = origFilePath.getParent().resolve(fileName + copyDescriptor + ".sgf");
    // we append -copy...-copy until we find a file
    // that doesn't exist yet and use that one
    while (Files.exists(outputFile)) {
      copyDescriptor = copyDescriptor + "-copy";
      outputFile = origFilePath.getParent().resolve(fileName + copyDescriptor + ".sgf");
    }

    File file = Sgf.writeToFile(game.getPositionSgf(move, vBoard));
    try {
      Files.move(Paths.get(file.toURI()), outputFile, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    System.out.println("Wrote position SGF to " + outputFile.toString());
    return true;
  }

  private HBox generateStatusBar() {
    HBox rtrn = new HBox();

    statusBarLabel = new Label("MainUI loaded");
    rtrn.getChildren().add(statusBarLabel);

    rtrn.setMinHeight(20);

    return rtrn;
  }

  public void updateStatus(String update) {
    this.statusBarLabel.setText(update);
  }

  public void initGame() {
    String game = "src/main/resources/game.sgf";
    Path path = Paths.get(game);
    // in development it is nice to have a game open on start
    if (path.toFile().exists()) {
      initializeGame(Paths.get(game));
    }
  }

  private VBox generateGameMetaInfoPane() {
    VBox vbox = new VBox();

    vbox.setMinWidth(250);
    GridPane pane = new GridPane();

    Label blackPlayerLabel = new Label("Black: ");
    GridPane.setConstraints(blackPlayerLabel, 1, 0);

    blackPlayerName = new Label("Unknown");
    GridPane.setConstraints(blackPlayerName, 2, 0);

    Label whitePlayerLabel = new Label("White: ");
    GridPane.setConstraints(whitePlayerLabel, 1, 1);

    whitePlayerName = new Label("Unknown");
    GridPane.setConstraints(whitePlayerName, 2, 1);

    pane.getChildren().addAll(blackPlayerLabel, blackPlayerName, whitePlayerLabel, whitePlayerName);

    TitledPane tPane = new TitledPane("Game Info", pane);
    vbox.getChildren().add(tPane);

    pane = new GridPane();
    Label label = new Label("Last opened: ");
    pane.add(label, 0, 0);

    folderInfoLastOpened = new Label();
    pane.add(folderInfoLastOpened, 1, 0);

    label = new Label("# of problems: ");
    pane.add(label, 0, 1);

    folderInfoNumberOfProblems = new Label();
    pane.add(folderInfoNumberOfProblems, 1, 1);

    label = new Label("# of solved: ");
    pane.add(label, 0, 2);

    folderInfoNumberOfSolved = new Label();
    pane.add(folderInfoNumberOfSolved, 1, 2);

    label = new Label("# of failed: ");
    pane.add(label, 0, 3);

    folderInfoNumberOfFailed = new Label();
    pane.add(folderInfoNumberOfFailed, 1, 3);

    label = new Label("Sort Order");
    label.getStyleClass().add("title-label");

    GridPane.setHalignment(label, HPos.CENTER);
    GridPane.setColumnSpan(label, 2);
    // pane.add(label, 1, 4);

    HBox hbox = new HBox();
    Button[] sortButtons = new Button[] { new Button("Name"), new Button("Difficulty") };
    for (int i = 0; i < sortButtons.length; i++) {
      Button btn = sortButtons[i];
      if (i == 0) {
        btn.getStyleClass().add("btn-selected");
      }
      hbox.getChildren().add(btn);
      btn.setOnAction(e -> {
        // MainUI.this.game.updateFileStatus(btn.getText());
        // resetProblemStatusButtonStyles();
        for (int j = 0; j < sortButtons.length; j++) {
          sortButtons[j].getStyleClass().remove("btn-selected");
        }
        btn.getStyleClass().add("btn-selected");
        // updateMetaInfoForGame(game);

        // trigger the event to update the icon
        // TreeItem<File> selectedItem =
        // fileTreeView.getSelectionModel().getSelectedItem();
        // Event.fireEvent(selectedItem, new
        // TreeModificationEvent<File>(TreeItem.<File>valueChangedEvent(), selectedItem,
        // selectedItem.getValue()));
      });
    }

    GridPane.setColumnSpan(hbox, 2);
    GridPane.setHalignment(hbox, HPos.CENTER);
    // pane.add(hbox, 0, 5);

    tPane = new TitledPane("Folder information", pane);
    vbox.getChildren().add(tPane);

    ///////////////////////////////////////////////
    pane = new GridPane();
    label = new Label("Last opened: ");
    GridPane.setConstraints(label, 1, 2);

    lastOpened = new Label("Never");
    GridPane.setConstraints(lastOpened, 2, 2);

    pane.getChildren().addAll(label, lastOpened);
    ///////////////////////////////////////////////
    label = new Label("Last solved: ");
    GridPane.setConstraints(label, 1, 3);

    lastSolved = new Label("Never");
    GridPane.setConstraints(lastSolved, 2, 3);

    pane.getChildren().addAll(label, lastSolved);

    label = new Label("Difficulty");
    label.getStyleClass().add("title-label");

    GridPane.setHalignment(label, HPos.CENTER);
    GridPane.setColumnSpan(label, 2);
    GridPane.setConstraints(label, 1, 4);
    pane.getChildren().addAll(label);

    hbox = new HBox();

    problemStatusButtons = new Button[] { new Button("None"), new Button("Easy"), new Button("Medium"),
        new Button("Difficult"), new Button("Fail"), };
    for (int i = 0; i < problemStatusButtons.length; i++) {
      Button btn = problemStatusButtons[i];
      hbox.getChildren().add(btn);
      btn.setOnAction(e -> {
        MainUI.this.game.updateFileStatus(btn.getText());
        resetProblemStatusButtonStyles();
        btn.getStyleClass().add("btn-selected");
        updateMetaInfoForGame(game);

        // trigger the event to update the icon
        TreeItem<File> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        Event.fireEvent(selectedItem,
            new TreeModificationEvent<File>(TreeItem.<File>valueChangedEvent(), selectedItem, selectedItem.getValue()));
      });
    }

    GridPane.setConstraints(hbox, 1, 5);
    GridPane.setColumnSpan(hbox, 2);
    pane.getChildren().addAll(hbox);

    tPane = new TitledPane("Problem Information", pane);
    vbox.getChildren().add(tPane);

    return vbox;
  }

  private TextArea generateCommentPane() {
    commentArea = new TextArea();
    commentArea.setFocusTraversable(false);
    commentArea.setWrapText(true);
    commentArea.setPrefSize(300, 600);

    commentArea.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue == null)
          return;
        if (newValue.trim().isEmpty())
          return;
        if (currentMove.getMoveNo() < 0) {
          if (Util.sgfEscapeText(newValue).equals(game.getProperty("C"))) {
            return;
          }
          game.setProperty("C", Util.sgfEscapeText(newValue));
        } else {
          if (Util.sgfEscapeText(newValue).equals(currentMove.getProperty("C"))) {
            return;
          }
          currentMove.addProperty("C", Util.sgfEscapeText(newValue));
        }
      }
    });

    return commentArea;
  }

  private void initializeGame(Path pathToSgf) {
    Font font = Font.getDefault();
    java.awt.Font awtFont = new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int) font.getSize());
    String encoding = TextUtils.determineEncoding(pathToSgf, awtFont);
    logger.debug("Determined encoding {}", encoding);
    updateStatus(String.format("Loaded %s with encoding %s", pathToSgf.getFileName(), encoding));

    this.activeGameSgf = pathToSgf;
    this.activeGameEncoding = encoding;
    this.game = new MyGame(Sgf.createFromPath(pathToSgf, encoding), pathToSgf);

    currentMove = game.getRootNode();

    prevMove = null;

    // reset our virtual board and actual board
    virtualBoard = new VirtualBoard();
    virtualBoard.addBoardListener(new GuiBoardListener(this));

    initNewBoard();
    reinitMoveTreePane();

    showMarkersForMove(game.getRootNode());
    showCommentForMove(game.getRootNode());

    updateMetaInfoForGame(this.game);
    // only now update the file opened, not to mess up the meta information
    this.game.updateFileOpened();

    treePaneScrollPane.setHvalue(0);
    treePaneScrollPane.setVvalue(0);

    moveNoField.setText("0");
    nextButton.requestFocus();
  }

  private void updateMetaInfoForGame(MyGame game) {
    String whiteRating = game.getProperty(SgfProperties.WHITE_PLAYER_RATING);
    String whiteLabel = game.getProperty(SgfProperties.WHITE_PLAYER_NAME);
    if (whiteRating != null) {
      whiteLabel = whiteLabel + " [" + whiteRating + "]";
    }
    if (game.getGame().getTimingInfoFound()) {
      MoveTimingInfo tmpTimings = game.getGame().getWTimings();
      whiteLabel += " (med " + tmpTimings.median + "s, max " + tmpTimings.max + "s)";
    }
    whitePlayerName.setText(whiteLabel);

    String blackRating = game.getProperty(SgfProperties.BLACK_PLAYER_RATING);
    String blackLabel = game.getProperty(SgfProperties.BLACK_PLAYER_NAME);
    if (blackRating != null) {
      blackLabel = blackLabel + " [" + blackRating + "]";
    }
    if (game.getGame().getTimingInfoFound()) {
      MoveTimingInfo tmpTimings = game.getGame().getBTimings();
      blackLabel += " (med " + tmpTimings.median + "s, max " + tmpTimings.max + "s)";
    }
    blackPlayerName.setText(blackLabel);

    this.folderInfoLastOpened.setText(game.getFolderInfoLastOpened());
    this.folderInfoNumberOfProblems.setText(game.getFolderInfoNumberOfProblems() + " SGF files");
    this.folderInfoNumberOfSolved.setText(game.getFolderInfoNumberOfSolved() + "");
    this.folderInfoNumberOfFailed.setText(game.getFolderInfoNumberOfFailed() + "");

    String lastOpened = game.getLastOpenedPretty();
    this.lastOpened.setText(lastOpened);
    String lastSolved = game.getLastSolvedPretty();
    this.lastSolved.setText(lastSolved);
    ProblemStatus problemStatus = game.getStatus();

    resetProblemStatusButtonStyles();
    problemStatusButtons[problemStatus.ordinal()].getStyleClass().add("btn-selected");
  }

  private void resetProblemStatusButtonStyles() {
    for (int i = 0; i < problemStatusButtons.length; i++) {
      problemStatusButtons[i].getStyleClass().removeAll("btn-selected");
    }
  }

  public void initNewBoard() {
    resetBoardPane(boardPane);
    placePreGameStones(game);
  }

  private void placePreGameStones(MyGame game) {
    String blackStones = game.getProperty("AB", "");
    String whiteStones = game.getProperty("AW", "");
    String removeStones = game.getProperty("AE", "");

    placePlacementGameStones(blackStones, whiteStones, removeStones);
  }

  private void placePlacementStones(GameNode node) {
    String blackStones = node.getProperty("AB", "");
    String whiteStones = node.getProperty("AW", "");
    String removeStones = node.getProperty("AE", "");

    placePlacementGameStones(blackStones, whiteStones, removeStones);
  }

  private void placePlacementGameStones(String addBlack, String addWhite, String stonesToBeRemoved) {
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

    if (stonesToBeRemoved.length() > 0) {
      String[] removeStones = stonesToBeRemoved.split(",");
      // actually the stones can also contain not just points but sequences
      // of points so instead of a coordinate like dq, dr, ds it might contain
      // dq:ds. Let us translate those to actual single coordinates!
      if (stonesToBeRemoved.contains(":")) {
        removeStones = Util.coordSequencesToSingle(stonesToBeRemoved);
      }

      for (int i = 0; i < removeStones.length; i++) {
        int[] moveCoords = Util.alphaToCoords(removeStones[i]);
        virtualBoard.placeStone(StoneState.EMPTY, moveCoords[0], moveCoords[1]);
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

  private void reinitMoveTreePane() {
    // I used to just getChildren().clear() but it produces problems
    // See
    // https://stackoverflow.com/questions/36862282/javafx-8-duplicate-children-after-getchildren-clear
    // So instead I'm generating a fresh pane
    movePane = generateMoveTreePane();
    treePaneScrollPane.setContent(movePane);

    GameStartNoopStone rootStone = new GameStartNoopStone(game.getRootNode());
    movePane.add(rootStone, 0, 0);

    nodeToTreeStone.clear();
    configureMoveTreeElement(game.getRootNode(), rootStone);

    populateMoveTreePane(game.getRootNode(), 0);
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
        } else if (nodeVisualDepth == -1) {
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
    nodeToTreeStone.put(node.getId(), treeStone);
    ((StackPane) treeStone).addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        MoveTreeElement stone = (MoveTreeElement) event.getSource();
        fastForwardTo(stone.getMove());
      }
    });
  }

  /*
   * Generates the boilerplate for the move tree pane. The pane is actually
   * populated during game initialization.
   */
  private ScrollPane generateMoveTreeScrollPane() {
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setMinHeight(100);
    scrollPane.setPrefHeight(175);
    scrollPane.setPrefWidth(640);
    scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

    return scrollPane;
  }

  private GridPane generateMoveTreePane() {
    GridPane gridPane = new GridPane();

    gridPane.setPadding(new Insets(0, 0, 0, 0));
    gridPane.setMinWidth(600);

    return gridPane;
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
    } else {
      virtualBoard.fastForwardTo(move);
    }
  }

  private VBox generateFileTreePane() {
    VBox vbox = new VBox();
    vbox.setMinWidth(250);

    fileTreeView = new FileTreeView();
    fileTreeView.setFocusTraversable(false);

    Label label = new Label("Choose SGF File");
    vbox.getChildren().addAll(label, fileTreeView);
    VBox.setVgrow(fileTreeView, Priority.ALWAYS);

    fileTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {

      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() == 2) {
          TreeItem<File> item = fileTreeView.getSelectionModel().getSelectedItem();
          File file = item.getValue().toPath().toFile();
          if (file.isFile()) {
            initializeGame(item.getValue().toPath());
            fileTreeView.startMonitoring(item.getValue().toPath().getParent());
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
      // scroll the move tree to make the highlighted move visible
      ensureVisibleForActiveTreeNode(currentMove);
    }
  }

  private void handleNextBranch() {
    if (currentMove.hasChildren()) {
      prevMove = currentMove;
      currentMove = currentMove.getChildren().iterator().next();
      virtualBoard.makeMove(currentMove, prevMove);
      // scroll the move tree to make the highlighted move visible
      ensureVisibleForActiveTreeNode(currentMove);
    } else {
      GameNode node = ParserUtils.findCorrespondingBelowMove(currentMove);
      if (node != null) {
        fastForwardTo(node);
      }
    }
  }

  public void handlePreviousPressed() {
    if (currentMove.getParentNode() != null) {
      prevMove = currentMove;
      currentMove = currentMove.getParentNode();

      virtualBoard.undoMove(prevMove, currentMove);
    }
  }

  private void handleUpPressed() {
    // we are on the top line already, nowhere to go
    if (currentMove.getVisualDepth() < 1) {
      return;
    }

    GameNode node = ParserUtils.findCorrespondingTopMove(currentMove);
    // only ff if such a move exists
    if (node != null) {
      fastForwardTo(node);
      ensureVisibleForActiveTreeNode(currentMove);
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

    if (move != null
        && (move.getProperty("AB") != null || move.getProperty("AW") != null || move.getProperty("AE") != null)) {
      placePlacementStones(move);
    }

    // handle the prev and new markers
    showMarkersForMove(move);

    String moveNo = move.getMoveNo() + "";
    if (move.getMoveNo() < 0)
      moveNo = "0";
    moveNoField.setText(moveNo);

    // show the associated comment
    showCommentForMove(move);
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

    String moveNo = prevMove.getMoveNo() + "";
    if (prevMove.getMoveNo() < 0)
      moveNo = "0";
    moveNoField.setText(moveNo);

    ensureVisibleForActiveTreeNode(prevMove);
  }

  private void ensureVisibleForActiveTreeNode(GameNode move) {
    if (move != null && move.isMove()) {
      StackPane stone = (StackPane) nodeToTreeStone.get(move.getId());

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
    MoveTreeElement stone = nodeToTreeStone.get(move.getId());
    // can remove the null check at one point when the
    // tree is fully implemented
    if (stone != null) {
      stone.highLight();
      stone.requestFocus();
      highlightedTreeStone.add(stone);
    } else {
      System.out.println("Not highlighting stone, can't find " + move.hashCode());
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
      } else {
        comment = "";
      }
    }

    comment = Util.sgfUnescapeText(comment);

    commentArea.setText(comment);
  }

  private void showMarkersForMove(GameNode move) {
    // the L property is actually not used in FF3 and FF4
    // but I own many SGFs that still have it
    String markerProp = move.getProperty("L");
    // the L property is sometimes attached to the game instead part instead
    if (markerProp == null && move.getParentNode() == null && game.getProperty("L") != null) {
      markerProp = game.getProperty("L");
    }
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
    // the L property is sometimes attached to the game instead part instead
    if (markerProp == null && node.getParentNode() == null && game.getProperty("L") != null) {
      markerProp = game.getProperty("L");
    }
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
        } else {
          BoardSquare btn = new BoardSquare(i, j);
          btn.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
          boardPane.add(btn, i, j);
          board[i - 1][j - 1] = btn;
        }
      }
    }
  }

  private void resetBoardPane(GridPane boardPane) {
    for (int i = 0; i < 19; i++) {
      for (int j = 0; j < 19; j++) {
        board[i][j].reset();
      }
    }
  }

  private int resizeBoardPane(GridPane boardPane, Bounds oldValue, Bounds newValue) {
    double width = newValue.getWidth() - leftVBox.getWidth() - rightVBox.getWidth();
    double height = newValue.getHeight() - treePaneScrollPane.getHeight() - buttonPane.getHeight();

    int minSize = (int) Math.min(width, height);
    int newSize = (int) Math.floor(minSize / 21);
    if ((int) Math.floor(minSize / 21) < newSize)
      newSize = (int) Math.floor(width / 21);

    // anything less than 29 will look ugly with the
    // current code
    if (newSize < 29) {
      newSize = 29;
    }

    // comparing to a random square - good enough, they are all the same size
    BoardSquare stone = (BoardSquare) boardPane.getChildren().get(23);
    // if size actually hasn't changed then no need to resize everything
    if (stone.getSize() == newSize) {
      return newSize;
    }

    for (int i = 0; i < 21 * 21; i++) {
      if ((i < 22 || i > 418 || i % 21 == 0 || (i + 1) % 21 == 0)) {
        BoardCoordinateLabel sq = (BoardCoordinateLabel) boardPane.getChildren().get(i);
        sq.resizeTo(newSize);
      } else {
        stone = (BoardSquare) boardPane.getChildren().get(i);
        stone.resizeTo(newSize);
      }

    }
    return newSize;
  }

  private void enableKeyboardShortcuts(Pane pane) {
  	pane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<Event>() {
  		@Override
      public void handle(Event genericEvent) {
      	KeyEvent event = (KeyEvent)genericEvent;
      	
        if (event.isMetaDown()) {
          return;
        }

        // wow, this is bad style but works right now
        if (commentArea.isFocused()) {
          return;
        }

        if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
          if (event.getCode().equals(KeyCode.LEFT)) {
            handlePreviousPressed();
          } else if (event.getCode().equals(KeyCode.RIGHT)) {
            handleNextPressed();
          } else if (event.getCode().equals(KeyCode.DOWN)) {
            handleNextBranch();
          } else if (event.getCode().equals(KeyCode.UP)) {
            handleUpPressed();
          } else if (event.getCode().equals(KeyCode.F2)) {
            fileTreeView.editSelectedItem();
          }
        }
  	  }
	});
  }

  public BoardSquare[][] getBoard() {
    return this.board;
  }

  public void fireUiVisibleEvent() {
    rootVbox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
      int newSize = resizeBoardPane(boardPane, oldValue, newValue);
      buttonPane.setPrefWidth(newSize * 21);
    });
  }

  @Override
  public void handle(MouseEvent event) {
    BoardSquare sq = (BoardSquare) event.getSource();
    if (sq.getState().isEmpty()) {
      String colorToPlay = "B";
      if (this.currentMove.isBlack()) {
        colorToPlay = "W";
      } else if (this.currentMove.isWhite()) {
        colorToPlay = "B";
      } else if (currentMove.getProperty("PL") != null) {
        colorToPlay = currentMove.getProperty("PL");
      } else if (game.getProperty("PL") != null) {
        colorToPlay = game.getProperty("PL");
      }

      // create move node
      int x = sq.getX() - 1;
      int y = sq.getY() - 1;
      GameNode move = null;

      if (currentMove.getNextNode() != null && currentMove.getNextNode().getMoveString() != null
          && currentMove.getNextNode().getCoords()[0] == x && currentMove.getNextNode().getCoords()[1] == y
          && currentMove.getNextNode().getColor().equals(colorToPlay)) {
        move = currentMove.getNextNode();
      }

      for (Iterator<GameNode> ite = currentMove.getChildren().iterator(); ite.hasNext();) {
        GameNode tmpNode = ite.next();
        if (tmpNode.getCoords()[0] == x && tmpNode.getCoords()[1] == y && tmpNode.getColor().equals(colorToPlay)
            && tmpNode.getColor().equals(colorToPlay)) {
          move = tmpNode;
          break;
        }
      }

      if (move == null) {
        move = new GameNode(this.currentMove);
        String coord = Util.coordToAlpha.get(x);
        coord += Util.coordToAlpha.get(y);
        move.addProperty(colorToPlay, coord);

        if (this.currentMove.getNextNode() != null) {
          this.currentMove.addChild(move);
        } else {
          this.currentMove.setNextNode(move);
          move.setPrevNode(this.currentMove);
        }

        game.getGame().postProcess();
        reinitMoveTreePane();
      }

      // play on the board
      virtualBoard.makeMove(move, this.currentMove);
    }
  }
}
