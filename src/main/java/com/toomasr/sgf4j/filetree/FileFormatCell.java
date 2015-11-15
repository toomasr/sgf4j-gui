package com.toomasr.sgf4j.filetree;

import java.io.File;

import javafx.scene.control.TreeCell;

public class FileFormatCell extends TreeCell<File> {
  public FileFormatCell() {
    super();
  }

  protected void updateItem(File item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    }
    else {
      /*
       * For a root device on a Mac getName will return an empty string
       * but actually I'd like to show a slash designating the root device
       *
       */
      if ("".equals(item.getName()))
        setText(item.getAbsolutePath());
      else
        setText(item.getName());
    }
  }
}
