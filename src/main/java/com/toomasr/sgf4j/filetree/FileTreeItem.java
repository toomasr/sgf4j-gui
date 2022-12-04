package com.toomasr.sgf4j.filetree;

import java.io.File;
import java.util.Arrays;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<File> {
  private boolean isLeaf;
  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;

  public FileTreeItem(File file) {
    super(file);

    expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        BooleanProperty bb = (BooleanProperty) observable;
        TreeItem<File> t = (TreeItem<File>) bb.getBean();

        // remove old children
        t.getChildren().clear();

        // add refreshed children
        ObservableList<TreeItem<File>> children = generateChildrenForItem(t);
        t.getChildren().addAll(children);
      }
    });
  }

  public void refresh() {
    isFirstTimeChildren = true;
    isFirstTimeLeaf = true;
    getChildren().clear();
    ObservableList<TreeItem<File>> children = generateChildrenForItem(this);
    getChildren().addAll(children);
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
        if (pathname.getAbsoluteFile().toString().toLowerCase().endsWith("sgf"))
          return true;
        return false;
      });

      if (files != null) {
        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
        Arrays.sort(files);
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
