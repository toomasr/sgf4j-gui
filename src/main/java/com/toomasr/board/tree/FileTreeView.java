package com.toomasr.board.tree;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.toomasr.sgf4j.gui.Sgf4jGuiUtil;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class FileTreeView extends TreeView<File> {
  public FileTreeView() {
    super();

    setMinWidth(250);
    getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
      @Override
      public TreeCell<File> call(TreeView<File> param) {
        return new FileFormatCell();
      }
    });

    final TreeItem<File> fakeRoot = new TreeItem<File>();
    setRoot(fakeRoot);
    setShowRoot(false);

    List<File> rootDevices = Sgf4jGuiUtil.getRootDevices();
    TreeItem<File>[] treeItems = new FileTreeItem[rootDevices.size()];
    for (int i = 0; i < rootDevices.size(); i++) {
      treeItems[i] = new FileTreeItem(rootDevices.get(i));
    }

    fakeRoot.getChildren().addAll(treeItems);

    findMatchinNodeFromView(fakeRoot.getChildren(), Sgf4jGuiUtil.getHomeFolder());
  }

  private void findMatchinNodeFromView(List<TreeItem<File>> children, File homeFolder) {
    for (Iterator<TreeItem<File>> ite = children.iterator(); ite.hasNext();) {
      TreeItem node = ite.next();
      System.out.println("Node "+node);
      //findMatchinNodeFromView(node.getChildren(), homeFolder);
    }
  }
}
