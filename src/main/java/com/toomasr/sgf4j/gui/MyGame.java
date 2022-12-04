package com.toomasr.sgf4j.gui;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import com.toomasr.sgf4j.metasystem.MetaSystem;
import com.toomasr.sgf4j.metasystem.ProblemStatus;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

public class MyGame {
  private Game game;
  private Path sgfPath;

  public MyGame(Game game, Path sgfPath) {
    this.game = game;
    this.sgfPath = sgfPath;
  }

  public GameNode getRootNode() {
    return this.game.getRootNode();
  }

  public Game getGame() {
    return this.game;
  }

  public String getProperty(String string) {
    return this.game.getProperty(string);
  }

  public String getProperty(String string, String def) {
    return this.game.getProperty(string, def);
  }
  
  public void setProperty(String key, String value) {
    this.game.setProperty(key, value);
  }

  public void updateFileOpened() {
    MetaSystem.updateFileOpened(sgfPath);
  }

  public void updateFileStatus(ProblemStatus status) {
    MetaSystem.updateFileStatus(sgfPath, status);
  }

  public String getLastOpenedPretty() {
    PrettyTime p = new PrettyTime();
    LocalDateTime lastOpened = MetaSystem.getLastOpened(sgfPath);
    if (lastOpened == null)
      return "Never";
    Date out = Date.from(lastOpened.atZone(ZoneId.systemDefault()).toInstant());
    return p.format(out);
  }

  public String getLastSolvedPretty() {
    PrettyTime p = new PrettyTime();
    LocalDateTime lastSolved = MetaSystem.getLastSolved(sgfPath);
    if (lastSolved == null)
      return "Never";
    Date out = Date.from(lastSolved.atZone(ZoneId.systemDefault()).toInstant());
    return p.format(out);
  }

  public ProblemStatus getStatus() {
    return MetaSystem.getStatus(sgfPath);
  }

  public void updateFileStatus(String text) {
    ProblemStatus status = Enum.valueOf(ProblemStatus.class, text.toUpperCase());
    updateFileStatus(status);
  }

  public String getFolderInfoLastOpened() {
    PrettyTime p = new PrettyTime();
    LocalDateTime lastSolved = MetaSystem.getFolderInfoLastOpened(sgfPath);
    if (lastSolved == null)
      return "Never";
    Date out = Date.from(lastSolved.atZone(ZoneId.systemDefault()).toInstant());
    return p.format(out);
  }

  public String getFolderInfoNumberOfProblems() {
    return MetaSystem.getFolderInfoNumberOfProblems(sgfPath);
  }

  public Integer getFolderInfoNumberOfSolved() {
    return MetaSystem.getFolderInfoNumberOfSolved(sgfPath);
  }

  public Integer getFolderInfoNumberOfFailed() {
    return MetaSystem.getFolderInfoNumberOfFailed(sgfPath);
  }
}
