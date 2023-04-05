package com.toomasr.sgf4j.filetree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.toomasr.sgf4j.metasystem.MetaSystem;
import com.toomasr.sgf4j.metasystem.ProblemStatus;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class FileFormatCell extends TreeCell<File> {
  private Image noneImage = new Image(getClass().getResourceAsStream("/icons/none_16x16.png"));
  private Image failedImage = new Image(getClass().getResourceAsStream("/icons/failed-red_16x16.png"));
  private Image solvedImage = new Image(getClass().getResourceAsStream("/icons/solved_16x16.png"));

  private ContextMenu contextMenu = new ContextMenu();
  private TextField textField;
  private File file;
  private FileTreeView fileTreeView;

  public FileFormatCell(FileTreeView fileTreeView) {
    super();

    this.fileTreeView = fileTreeView;
    fileTreeView.registerFileFormatCell(this);

    MenuItem renameItem = new MenuItem("Rename");
    renameItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        startEdit();
      }
    });

    contextMenu.getItems().add(renameItem);

    MenuItem deleteItem = new MenuItem("Delete");
    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        delete();
      }

    });

    contextMenu.getItems().add(deleteItem);
  }

  protected void delete() {
    try {
      Files.delete(this.file.toPath());
      fileTreeView.refreshFolder(this.file.toPath().getParent());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void updateItem(File file, boolean empty) {
    super.updateItem(file, empty);

    this.file = file;
    if (empty || file == null) {
      setText(null);
      setGraphic(null);
    } else {
      if (!isEditing() && getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
        setContextMenu(contextMenu);
      }
      /*
       * For a root device on a Mac getName will return an empty string but actually
       * I'd like to show a slash designating the root device
       *
       */
      if ("".equals(file.getName()))
        setText(file.getAbsolutePath());
      /*
       * For SGF files we want to show some custom icons.
       */
      else if (file != null && file.isFile() && file.toString().toLowerCase().endsWith("sgf")) {
        setText(file.getName());
        if (MetaSystem.systemExists(file.toPath())) {
          ProblemStatus status = MetaSystem.getStatus(file.toPath());

          if (status == ProblemStatus.NONE)
            setGraphic(new ImageView(noneImage));
          else if (status == ProblemStatus.FAIL)
            setGraphic(new ImageView(failedImage));
          else
            setGraphic(new ImageView(solvedImage));
        } else {
          setGraphic(null);
        }
      } else {
        setText(file.getName());
        setGraphic(null);
      }
    }
  }

  @Override
  public void startEdit() {
    super.startEdit();

    if (textField == null) {
      createTextField();
    }
    setText(null);
    setGraphic(textField);
    textField.selectAll();
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    updateItem(file, isEmpty());
  }

  @Override
  public void commitEdit(File file) {
    super.commitEdit(file);
    this.file = file;
  }

  public File getFile() {
    return file;
  }
  
  public void commitEdit(String text) {
    Path source = file.toPath();
    Path target = file.toPath().resolveSibling(text);
    try {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      commitEdit(target.toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createTextField() {
    textField = new TextField(file.getName());
    textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent t) {
        if (t.getCode() == KeyCode.ENTER) {
          commitEdit(textField.getText());
        } else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        } else {
        	System.out.println("Debug - what is wrong " + t.getText());
        }
      }
    });
  }
}
