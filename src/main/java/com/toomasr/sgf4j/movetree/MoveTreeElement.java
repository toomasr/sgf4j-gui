package com.toomasr.sgf4j.movetree;

import com.toomasr.sgf4j.parser.GameNode;

public interface MoveTreeElement {
  public void highLight();

  public void deHighLight();

  public void requestFocus();
  public GameNode getMove();
}
