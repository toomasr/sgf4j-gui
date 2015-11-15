package com.toomasr.board.filetree;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<File> {
  private boolean isLeaf;
  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;

  public FileTreeItem(File file) {
    super(file);
  }

  @Override
  public ObservableList<TreeItem<File>> getChildren() {
    if (isFirstTimeChildren) {
      isFirstTimeChildren = false;
      super.getChildren().setAll(generateChildrenForItem(this));
    }
    return super.getChildren();
  }

  private ObservableList<TreeItem<File>> generateChildrenForItem(TreeItem<File> TreeItem) {
    File f = TreeItem.getValue();
    if (f != null && f.isDirectory()) {
      File[] files = f.listFiles((pathname) -> {
        if (pathname.getName().startsWith("."))
          return false;
        if (pathname.isDirectory())
          return true;
        if (pathname.getAbsoluteFile().toString().endsWith("sgf"))
          return true;
        return false;
      });
      
      if (files != null) {
        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

        for (File childFile : files) {
          FileTreeItem fileTreeItem = new FileTreeItem(childFile);
          children.add(fileTreeItem);
        }
        return children;
      }
    }

    return FXCollections.emptyObservableList();
  }

  @Override
  public boolean isLeaf() {
    if (isFirstTimeLeaf) {
      isFirstTimeLeaf = false;
      File f = (File) getValue();
      isLeaf = f.isFile();
    }

    return isLeaf;
  }

  @Override
  public String toString() {
    return getValue().getName();
  }
}
