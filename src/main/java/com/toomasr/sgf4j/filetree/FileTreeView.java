package com.toomasr.sgf4j.filetree;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.toomasr.sgf4j.gui.Sgf4jGuiUtil;
import com.toomasr.sgf4j.properties.AppState;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class FileTreeView extends TreeView<File> {
  private FileChangesWatcher fileChangesWatcher;
  private TreeItem<File> fakeRoot;
  private Thread fileChangesWatcherThread;
  private Set<FileFormatCell> fileFormatCells = new HashSet<>();

  public FileTreeView() {
    super();

    getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    setEditable(false);
    setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
      @Override
      public TreeCell<File> call(TreeView<File> param) {
        return new FileFormatCell(FileTreeView.this);
      }
    });

    fakeRoot = new TreeItem<File>();
    setRoot(fakeRoot);
    setShowRoot(false);

    List<File> rootDevices = Sgf4jGuiUtil.getRootDevices();
    TreeItem<File>[] treeItems = new FileTreeItem[rootDevices.size()];
    for (int i = 0; i < rootDevices.size(); i++) {
      treeItems[i] = new FileTreeItem(rootDevices.get(i));
    }

    fakeRoot.getChildren().addAll(treeItems);

    openTreeAtRightLocation(fakeRoot);

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent event) {
      }
    });
  }

  /**
   * We'll try to open the tree at the last saved location. If there is no last
   * saved location or it doesn't exist then we'll open at the home folder
   * reported by {@link Sgf4jGuiUtil}.
   *
   * @param fakeRoot the root of the TreeView
   */
  private void openTreeAtRightLocation(final TreeItem<File> fakeRoot) {
    File rightLocation = Sgf4jGuiUtil.getAppHomeFolder();
    String fileToOpen = AppState.getInstance().getProperty(AppState.CURRENT_FILE);
    if (fileToOpen != null) {
      File tmpFile = new File(fileToOpen);
      if (tmpFile.exists()) {
        rightLocation = tmpFile;
      } else if (tmpFile.toPath().getParent().toFile().exists()) {
        rightLocation = tmpFile.toPath().getParent().toFile();
      }
    }

    TreeItem<File> treeItem = findMatchingTreeItemFromView(fakeRoot.getChildren(), rightLocation);
    getSelectionModel().select(treeItem);
    scrollTo(getSelectionModel().getSelectedIndex());
  }

  private FileTreeItem findMatchingTreeItemFromView(List<TreeItem<File>> root, final File homeFolder) {
    List<String> pathElems = tokenizePath(homeFolder);
    TreeItem<File> rtrn = null;

    for (int i = pathElems.size() - 1; i > -1; i--) {
      String token = pathElems.get(i);

      List<TreeItem<File>> children = null;
      for (Iterator<TreeItem<File>> ite = root.iterator(); ite.hasNext();) {
        TreeItem<File> node = ite.next();
        if (token.equals(node.getValue().getName())) {
          node.setExpanded(true);
          rtrn = node;

          children = node.getChildren();
          break;
        }
      }

      // we didn't find a match, no point in looking further
      if (children == null) {
        break;
      }

      // now lets go a level deeper
      root = children;
    }
    return (FileTreeItem) rtrn;
  }

  private List<String> tokenizePath(File homeFolder) {
    File file = new File(homeFolder.getAbsolutePath());
    List<String> pathElems = new ArrayList<>();
    do {
      pathElems.add(file.getName());
    } while ((file = file.getParentFile()) != null);
    return pathElems;
  }

  public void startMonitoring(Path path) {
    if (fileChangesWatcher != null) {
      if (fileChangesWatcher.getPath().equals(path)) {
        return;
      }

      fileChangesWatcher.stop();
      fileChangesWatcherThread.interrupt();
    }

    fileChangesWatcher = new FileChangesWatcher(this, path);
    fileChangesWatcherThread = new Thread(fileChangesWatcher);
    fileChangesWatcherThread.setDaemon(true);
    fileChangesWatcherThread
        .setName("file watcher - " + path.getFileName().toString() + " " + (int) (Math.random() * 100));
    fileChangesWatcherThread.start();
  }

  public void refreshFolder(Path path) {
    FileTreeItem parentTreeItem = this.findMatchingTreeItemFromView(fakeRoot.getChildren(), path.toFile());
    Platform.runLater(() -> {
      parentTreeItem.refresh();
      getSelectionModel().select(parentTreeItem);
    });
  }

  public void editSelectedItem() {
    FileTreeItem selectedItem = (FileTreeItem) getSelectionModel().getSelectedItem();
    for (FileFormatCell fileFormatCell : fileFormatCells) {
      if (selectedItem.getValue().equals(fileFormatCell.getFile())) {
        fileFormatCell.startEdit();
      }
    }
  }

  public void registerFileFormatCell(FileFormatCell fileFormatCell) {
    this.fileFormatCells.add(fileFormatCell);
  }
}
