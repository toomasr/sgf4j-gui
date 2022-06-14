package com.toomasr.sgf4j.board;

import com.toomasr.sgf4j.gui.MainUI;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.board.BoardListener;
import com.toomasr.sgf4j.parser.board.StoneState;

public class GuiBoardListener implements BoardListener {
  private BoardSquare[][] board;
  private MainUI mainUI;

  public GuiBoardListener(MainUI mainUI) {
    this.board = mainUI.getBoard();
    this.mainUI = mainUI;
  }

  @Override
  public void placeStone(int x, int y, StoneState stoneState) {
    this.board[x][y].placeStone(stoneState);
  }

  @Override
  public void removeStone(int x, int y) {
    this.board[x][y].removeStone();
  }

  @Override
  public void playMove(GameNode node, GameNode prevMove) {
    mainUI.playMove(node, prevMove);
  }

  @Override
  public void undoMove(GameNode move, GameNode prevMove) {
    mainUI.undoMove(move, prevMove);
  }

  @Override
  public void initInitialPosition() {
    mainUI.initNewBoard();
  }
}
